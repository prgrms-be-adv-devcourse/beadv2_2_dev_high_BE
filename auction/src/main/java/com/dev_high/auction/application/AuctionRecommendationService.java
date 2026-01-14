package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionRecommendationResponse;
import com.dev_high.auction.application.dto.AuctionRecommendationResponse.AuctionAiRecommendationResult;
import com.dev_high.auction.application.dto.ProductInfoSummary;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.dto.SimilarProductResponse;
import com.dev_high.common.dto.WinningOrderRecommendationResponse;
import com.dev_high.config.AuctionRecommendationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionRecommendationService {

    private final RestTemplate restTemplate;
    private final AuctionRecommendationProperties properties;
    private final AuctionRepository auctionRepository;
    private final AuctionRecommendationAiService auctionRecommendationAiService;
    private final AuctionRecommendationCacheService cacheService;
    private final AuctionRecommendationAssembler assembler;

    public AuctionRecommendationResponse recommend(String productId) {
        AuctionRecommendationResponse cached = cacheService.get(productId);
        if (cached != null) {
            return cached;
        }

        ProductInfoSummary productInfo = fetchProductInfo(productId);
        List<SimilarProductResponse> similars = fetchSimilarProducts(productId);
        log.info("recommendation: productId={}, similars={}", productId, similars.size());
        double minSimilarity = properties.getMinSimilarity();
        if (minSimilarity > 0.0) {
            similars = similars.stream()
                    .filter(item -> item.score() >= minSimilarity)
                    .toList();
        }
        List<String> similarProductIds = buildSimilarProductIds(similars);
        log.info("recommendation: similarProductIds={}", similarProductIds);
        List<String> auctionCountIds = buildAuctionCountIds(productId, similars);
        int auctionCount = countAuctions(auctionCountIds);
        log.info("recommendation: auctionCountIds={}, auctionCount={}", auctionCountIds, auctionCount);

        if (similars.isEmpty()) {
            AuctionRecommendationResponse response = assembler.baseResponse(
                    productId,
                    false,
                    "유사 상품 데이터가 없습니다.",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    0,
                    auctionCount,
                    0
            );
            return finalizeAndCache(productId, response, productInfo);
        }

        List<BigDecimal> auctionStartBids = fetchAuctionStartBids(similarProductIds);
        PriceSummary auctionStartBidSummary = summarizePrices(auctionStartBids);
        PriceRange auctionStartBidRange = summarizeRange(auctionStartBids);

        List<WinningOrderRecommendationResponse> orders = fetchWinningOrders(similarProductIds);

        if (orders.isEmpty()) {
            BigDecimal startBidBaseline = roundToHundreds(calculateStartBidFloor(auctionStartBids));

            BigDecimal recommendedStartBid = roundToHundreds(buildStartBid(null, startBidBaseline));
            BigDecimal rangeMin = auctionStartBidRange == null ? null : auctionStartBidRange.min();
            BigDecimal rangeMax = auctionStartBidRange == null ? null : auctionStartBidRange.max();
            if (rangeMin == null || rangeMax == null) {
                if (recommendedStartBid != null) {
                    double percent = Math.max(0.0, properties.getRangePercent());
                    rangeMin = roundToHundreds(recommendedStartBid.multiply(BigDecimal.valueOf(1.0 - percent)));
                    rangeMax = roundToHundreds(recommendedStartBid.multiply(BigDecimal.valueOf(1.0 + percent)));
                }
            }

            AuctionRecommendationResponse response = assembler.baseResponse(
                    productId,
                    true,
                    "낙찰 데이터가 없어 유사 경매 시작가를 기준으로 추천했습니다.",
                    null,
                    recommendedStartBid,
                    rangeMin,
                    rangeMax,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    auctionStartBidSummary == null ? null : auctionStartBidSummary.min(),
                    auctionStartBidSummary == null ? null : auctionStartBidSummary.max(),
                    auctionStartBidSummary == null ? null : auctionStartBidSummary.avg(),
                    auctionStartBidSummary == null ? null : auctionStartBidSummary.median(),
                    similars.size(),
                    0,
                    auctionCount,
                    0
            );
            return finalizeAndCache(productId, response, productInfo);
        }

        Map<String, Double> similarityMap = similars.stream()
                .collect(Collectors.toMap(SimilarProductResponse::productId, SimilarProductResponse::score));

        PriceSummary winningSummary = summarizePrices(
                orders.stream()
                        .map(WinningOrderRecommendationResponse::winningAmount)
                        .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                        .toList()
        );
        BigDecimal referencePrice = roundToHundreds(calculateReferencePrice(orders, similarityMap));
        BigDecimal startBidBaseline = roundToHundreds(calculateStartBidFloor(auctionStartBids));
        int paidLikeCount = countPaidLikeOrders(orders);
        BigDecimal recommendedStartBid = roundToHundreds(buildStartBid(referencePrice, startBidBaseline));
        BigDecimal rangeMin = auctionStartBidRange == null ? null : auctionStartBidRange.min();
        BigDecimal rangeMax = auctionStartBidRange == null ? null : auctionStartBidRange.max();
        if (rangeMin == null || rangeMax == null) {
            if (recommendedStartBid != null) {
                double percent = Math.max(0.0, properties.getRangePercent());
                rangeMin = roundToHundreds(recommendedStartBid.multiply(BigDecimal.valueOf(1.0 - percent)));
                rangeMax = roundToHundreds(recommendedStartBid.multiply(BigDecimal.valueOf(1.0 + percent)));
            }
        }
        if (referencePrice != null && rangeMax != null && rangeMax.compareTo(referencePrice) < 0) {
            rangeMax = roundToHundreds(referencePrice);
        }

        OffsetDateTime recommendedStartAt = null;
        OffsetDateTime recommendedEndAt = null;

        AuctionRecommendationResponse response = assembler.baseResponse(
                productId,
                true,
                "OK",
                referencePrice,
                recommendedStartBid,
                rangeMin,
                rangeMax,
                recommendedStartAt,
                recommendedEndAt,
                winningSummary == null ? null : winningSummary.min(),
                winningSummary == null ? null : winningSummary.max(),
                winningSummary == null ? null : winningSummary.avg(),
                winningSummary == null ? null : winningSummary.median(),
                auctionStartBidSummary == null ? null : auctionStartBidSummary.min(),
                auctionStartBidSummary == null ? null : auctionStartBidSummary.max(),
                auctionStartBidSummary == null ? null : auctionStartBidSummary.avg(),
                auctionStartBidSummary == null ? null : auctionStartBidSummary.median(),
                similars.size(),
                orders.size(),
                auctionCount,
                paidLikeCount
        );
        return finalizeAndCache(productId, response, productInfo);
    }

    private BigDecimal buildStartBid(BigDecimal winningReference, BigDecimal auctionBaseline) {
        if (winningReference == null && auctionBaseline == null) {
            return null;
        }

        BigDecimal reference = winningReference == null ? auctionBaseline : winningReference;
        if (auctionBaseline != null && winningReference != null) {
            double winWeight = Math.max(0.0, properties.getWinningBlendWeight());
            double auctionWeight = Math.max(0.0, properties.getAuctionBlendWeight());
            double sum = winWeight + auctionWeight;
            if (sum > 0.0) {
                double normalizedWin = winWeight / sum;
                double normalizedAuction = auctionWeight / sum;
                reference = winningReference.multiply(BigDecimal.valueOf(normalizedWin))
                        .add(auctionBaseline.multiply(BigDecimal.valueOf(normalizedAuction)));
            }
        }

        return reference.multiply(BigDecimal.valueOf(properties.getStartBidRatio()))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private List<SimilarProductResponse> fetchSimilarProducts(String productId) {
        String url = "http://SEARCH-SERVICE/api/v1/search/similar?productId=%s&limit=%d"
                .formatted(productId, properties.getSimilarLimit());

        try {
            ResponseEntity<ApiResponseDto<List<SimilarProductResponse>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            ApiResponseDto<List<SimilarProductResponse>> body = response.getBody();
            if (body == null || body.getData() == null) {
                return List.of();
            }
            return body.getData();
        } catch (Exception e) {
            log.warn("failed to fetch similar products: {}", e.getMessage());
            return List.of();
        }
    }

    private ProductInfoSummary fetchProductInfo(String productId) {
        if (productId == null || productId.isBlank()) {
            return null;
        }
        String url = "http://PRODUCT-SERVICE/api/v1/products/" + productId;
        try {
            ResponseEntity<ApiResponseDto<ProductInfoSummary>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            ApiResponseDto<ProductInfoSummary> body = response.getBody();
            return body == null ? null : body.getData();
        } catch (Exception e) {
            log.warn("failed to fetch product info: {}", e.getMessage());
            return null;
        }
    }

    private List<WinningOrderRecommendationResponse> fetchWinningOrders(List<String> productIds) {
        String joined = String.join(",", productIds);
        String url = "http://SETTLEMENT-SERVICE/api/v1/orders/winning/recommendation"
                + "?productIds=" + joined
                + "&limit=" + properties.getWinningLimit()
                + "&days=" + properties.getLookbackDays();

        try {
            ResponseEntity<ApiResponseDto<List<WinningOrderRecommendationResponse>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            ApiResponseDto<List<WinningOrderRecommendationResponse>> body = response.getBody();
            if (body == null || body.getData() == null) {
                return List.of();
            }
            return body.getData();
        } catch (Exception e) {
            log.warn("failed to fetch winning orders: {}", e.getMessage());
            return List.of();
        }
    }

    private BigDecimal calculateReferencePrice(
            List<WinningOrderRecommendationResponse> orders,
            Map<String, Double> similarityMap
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (WinningOrderRecommendationResponse order : orders) {
            double similarity = similarityMap.getOrDefault(order.productId(), 0.0);
            if (similarity <= 0.0 || order.winningAmount() == null || order.winningDate() == null) {
                continue;
            }
            double days = Math.max(0.0, Duration.between(order.winningDate(), now).toHours() / 24.0);
            double timeWeight = Math.exp(-days / properties.getTimeDecayDays());
            double statusWeight = resolveStatusWeight(order.status());
            double weight = similarity * timeWeight * statusWeight;
      weightedSum += weight * order.winningAmount().doubleValue();
            totalWeight += weight;
        }

        if (totalWeight > 0.0) {
            return BigDecimal.valueOf(weightedSum / totalWeight).setScale(0, RoundingMode.HALF_UP);
        }

    List<BigDecimal> amounts = orders.stream()
        .map(WinningOrderRecommendationResponse::winningAmount)
        .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
        .sorted()
        .toList();

        if (amounts.isEmpty()) {
            return null;
        }

    int mid = amounts.size() / 2;
    BigDecimal median = amounts.size() % 2 == 0
        ? amounts.get(mid - 1).add(amounts.get(mid))
            .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
        : amounts.get(mid);
    return median;
  }

    private BigDecimal calculateStartBidFloor(List<BigDecimal> bids) {
        List<BigDecimal> filteredBids = bids == null ? List.of() : bids.stream()
                .filter(bid -> bid != null && bid.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .toList();

        if (filteredBids.isEmpty()) {
            return null;
        }

        List<Double> values = filteredBids.stream().map(BigDecimal::doubleValue).sorted().toList();
        double q1 = percentile(values, 0.25);
        double q3 = percentile(values, 0.75);
        double iqr = q3 - q1;
        double lower = q1 - (1.5 * iqr);
        double upper = q3 + (1.5 * iqr);

        List<Double> filtered = values.stream()
                .filter(v -> v >= lower && v <= upper)
                .toList();

        List<Double> target = filtered.isEmpty() ? values : filtered;
        double median = percentile(target, 0.5);
        return BigDecimal.valueOf(median).setScale(0, RoundingMode.HALF_UP);
    }

    private List<BigDecimal> fetchAuctionStartBids(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        List<Auction> auctions = auctionRepository.findByProductIdIn(productIds);
        return auctions.stream()
                .map(Auction::getStartBid)
                .filter(bid -> bid != null && bid.compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    private PriceSummary summarizePrices(List<BigDecimal> amounts) {
        List<BigDecimal> values = amounts == null ? List.of() : amounts.stream()
                .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal min = values.get(0);
        BigDecimal max = values.get(values.size() - 1);
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            sum = sum.add(value);
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(values.size()), 0, RoundingMode.HALF_UP);
        int mid = values.size() / 2;
        BigDecimal median = values.size() % 2 == 0
                ? values.get(mid - 1).add(values.get(mid))
                .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
                : values.get(mid);
        return new PriceSummary(
                roundToHundreds(min),
                roundToHundreds(max),
                roundToHundreds(avg),
                roundToHundreds(median)
        );
    }

    private PriceRange summarizeRange(List<BigDecimal> amounts) {
        List<BigDecimal> values = amounts == null ? List.of() : amounts.stream()
                .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .toList();
        if (values.isEmpty()) {
            return null;
        }
        List<Double> raw = values.stream().map(BigDecimal::doubleValue).sorted().toList();
        double q1 = percentile(raw, 0.25);
        double q3 = percentile(raw, 0.75);
        double iqr = q3 - q1;
        double lower = q1 - (1.5 * iqr);
        double upper = q3 + (1.5 * iqr);
        List<Double> filtered = raw.stream()
                .filter(v -> v >= lower && v <= upper)
                .toList();
        List<Double> target = filtered.isEmpty() ? raw : filtered;
        double min = target.get(0);
        double max = target.get(target.size() - 1);
        double percent = Math.max(0.0, properties.getRangePercent());
        if (percent > 0.0) {
            min = min * (1.0 - percent);
            max = max * (1.0 + percent);
        }
        return new PriceRange(
                roundToHundreds(BigDecimal.valueOf(min)),
                roundToHundreds(BigDecimal.valueOf(max))
        );
    }

    private BigDecimal roundToHundreds(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal hundred = BigDecimal.valueOf(100);
        return value.divide(hundred, 0, RoundingMode.HALF_UP).multiply(hundred);
    }

    private int countAuctions(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        return auctionRepository.findByProductIdIn(productIds).size();
    }

    private int countPaidLikeOrders(List<WinningOrderRecommendationResponse> orders) {
        int paidLike = 0;
        if (orders != null) {
            for (WinningOrderRecommendationResponse order : orders) {
                String status = order.status();
                if (status == null) {
                    continue;
                }
                switch (status) {
                    case "PAID", "SHIP_STARTED", "SHIP_COMPLETED", "CONFIRM_BUY" -> paidLike++;
                    default -> {
                    }
                }
            }
        }
        return paidLike;
    }

    private double percentile(List<Double> values, double percentile) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double position = percentile * (values.size() - 1);
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);
        if (lowerIndex == upperIndex) {
            return values.get(lowerIndex);
        }
        double lower = values.get(lowerIndex);
        double upper = values.get(upperIndex);
        return lower + (upper - lower) * (position - lowerIndex);
    }

    private double resolveStatusWeight(String status) {
        if (status == null) {
            return 1.0;
        }
        String key = status.toLowerCase().replace('_', '-');
        return properties.getStatusWeight().getOrDefault(key, 1.0);
    }

    private List<String> buildSimilarProductIds(List<SimilarProductResponse> similars) {
        Set<String> ids = new LinkedHashSet<>();
        similars.forEach(item -> ids.add(item.productId()));
        return ids.stream().toList();
    }

    private List<String> buildAuctionCountIds(String productId, List<SimilarProductResponse> similars) {
        Set<String> ids = new LinkedHashSet<>();
        if (productId != null && !productId.isBlank()) {
            ids.add(productId);
        }
        similars.forEach(item -> ids.add(item.productId()));
        return ids.stream().toList();
    }

    private AuctionRecommendationResponse finalizeAndCache(
            String productId,
            AuctionRecommendationResponse response,
            ProductInfoSummary productInfo
    ) {
        var aiResult = auctionRecommendationAiService.buildResult(response, productInfo);
        AuctionRecommendationResponse finalResponse = withAiOptional(response, aiResult);
        if (aiResult != null) {
            cacheService.put(productId, finalResponse);
        }
        return finalResponse;
    }

    private AuctionRecommendationResponse withAiOptional(
            AuctionRecommendationResponse base,
            AuctionAiRecommendationResult aiResult
    ) {
        if (aiResult == null) {
            return base;
        }
        AuctionAiRecommendationResult normalized = aiResult.price() == null
                ? aiResult
                : new AuctionAiRecommendationResult(roundToHundreds(aiResult.price()), aiResult.reason());
        return assembler.withAi(base, normalized);
    }

    private record PriceSummary(
            BigDecimal min,
            BigDecimal max,
            BigDecimal avg,
            BigDecimal median
    ) {
    }

    private record PriceRange(
            BigDecimal min,
            BigDecimal max
    ) {
    }
}
