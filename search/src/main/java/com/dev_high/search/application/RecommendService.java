package com.dev_high.search.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.MgetRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.search.application.ai.AiRecommendationSummaryGenerator;
import com.dev_high.search.application.dto.ProductRecommendResponse;
import com.dev_high.search.application.dto.ProductRecommendSummaryResponse;
import com.dev_high.search.application.dto.RecommendationConfidence;
import com.dev_high.search.application.mapper.ProductRecommendMapper;
import com.dev_high.search.util.VectorUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private static final String INDEX = "product";
    private static final int EMBEDDING_DIMS = 1536;

    private static final int LIMIT = 5;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ElasticsearchClient elasticsearchClient;
    private final AiRecommendationSummaryGenerator summaryGenerator;

    public List<ProductRecommendResponse> recommendByWishlist(Set<String> wishlistProductIds) {
        String userId = UserContext.get().userId();

        if (wishlistProductIds.isEmpty()) {
            return fallback(wishlistProductIds, userId);
        }

        List<float[]> vectors = fetchEmbeddings(wishlistProductIds);
        if (vectors.isEmpty()) {
            return fallback(wishlistProductIds, userId);
        }

        float[] userVector = VectorUtils.meanVector(vectors);

        VectorUtils.l2NormalizeInPlace(userVector);

        List<ProductRecommendResponse> candidates = searchCandidatesByKnn(userVector, wishlistProductIds, userId);

        if (candidates.isEmpty()) {
            return fallback(wishlistProductIds, userId);
        }

        return candidates;
    }

    public ApiResponseDto<ProductRecommendSummaryResponse> recommendByWishlistWithSummary(List<String> wishlistProductIds) {
        Set<String> wishlistIds = (wishlistProductIds == null)
                ? Set.of()
                : wishlistProductIds.stream()
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        List<ProductRecommendResponse> items = recommendByWishlist(wishlistIds);
        RecommendationConfidence confidence = calculateConfidence(wishlistIds, items);

        String summary;
        switch (confidence) {
            case MID -> summary = midConfidenceSummary();
            case HIGH -> summary = summaryGenerator.summarize(items, midConfidenceSummary());
            default -> summary = baseRecommendationSummary();
        }

        return ApiResponseDto.success(
                new ProductRecommendSummaryResponse(summary, items)
        );
    }

    private RecommendationConfidence calculateConfidence(
            Set<String> wishlistIds,
            List<ProductRecommendResponse> results
    ) {
        if (wishlistIds.isEmpty() || results == null || results.isEmpty()) {
            return RecommendationConfidence.LOW;
        }

        double avgScore = results.stream()
                .map(ProductRecommendResponse::score)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        if (avgScore < 0.88) {
            return RecommendationConfidence.LOW;
        }
        if (avgScore < 0.92) {
            return RecommendationConfidence.MID;
        }
        return RecommendationConfidence.HIGH;
    }


    private String baseRecommendationSummary() {
        return "오늘의 추천 경매 상품을 모아봤어요.";
    }

    private String midConfidenceSummary() {
        return "찜한 상품과 일부 비슷한 흐름의 상품을 추천했어요.";
    }

    private List<ProductRecommendResponse> searchCandidatesByKnn(
            float[] userVector,
            Set<String> wishlistIds,
            String userId
    ) {
        int k = LIMIT * LIMIT;
        int numCandidates = 200;

        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(INDEX)
                    .size(LIMIT)
                    .knn(knn -> knn
                            .field("embedding")
                            .queryVector(VectorUtils.toFloatList(userVector))
                            .k(k)
                            .numCandidates(numCandidates)
                    )
                    .minScore(0.80)
                    .query(q -> q.bool(b -> {
                        b.filter(f -> f.exists(e -> e.field("embedding")));
                        return b;
                    }))
                    .postFilter(pf -> pf.bool(b -> {
                        applyRecommendFilters(b, wishlistIds, userId);
                        return b;
                    }))
            );

            var resp = elasticsearchClient.search(req, JsonNode.class);

            return resp.hits().hits().stream()
                    .map(ProductRecommendMapper::from)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.error("kNN 추천 검색 실패", e);
            return List.of();
        }
    }

    private List<ProductRecommendResponse> fallback(
            Set<String> wishlistIds,
            String userId
    ) {
        try {
            long seed = dailySeed(userId);

            SearchRequest req = SearchRequest.of(s -> s
                    .index(INDEX)
                    .size(LIMIT)
                    .query(q -> q.functionScore(fs -> fs
                            .query(qq -> qq.bool(b -> {
                                b.must(m -> m.matchAll(ma -> ma));
                                applyRecommendFilters(b, wishlistIds, userId);
                                return b;
                            }))
                            .functions(f -> f.randomScore(rs -> rs
                                    .seed(String.valueOf(seed))
                                    .field("_seq_no")
                            ))
                            .boostMode(co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode.Replace)
                    ))
            );

            var resp = elasticsearchClient.search(req, JsonNode.class);

            return resp.hits().hits().stream()
                    .map(ProductRecommendMapper::from)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.warn("기본 추천(fallback) 수행 중 오류 발생", e);
            return List.of();
        }
    }

    private void applyRecommendFilters(
            BoolQuery.Builder b,
            Set<String> excludeProductIds,
            String userId
    ) {
        if (excludeProductIds != null && !excludeProductIds.isEmpty()) {
            b.mustNot(mn -> mn.terms(t -> t
                    .field("productId")
                    .terms(v -> v.value(excludeProductIds.stream().map(FieldValue::of).toList()))
            ));
        }

        if (userId != null && !userId.isBlank()) {
            b.mustNot(mn -> mn.term(t -> t.field("sellerId").value(userId)));
        }

        b.mustNot(mn -> mn.term(t -> t.field("status").value("COMPLETED")));
    }

    private List<float[]> fetchEmbeddings(Set<String> ids) {
        try {
            MgetRequest mget = MgetRequest.of(m -> m.index(INDEX).ids(new ArrayList<>(ids)));
            var resp = elasticsearchClient.mget(mget, JsonNode.class);

            List<float[]> vectors = new ArrayList<>();

            for (MultiGetResponseItem<JsonNode> item : resp.docs()) {
                if (!item.isResult()) {
                    continue;
                }

                JsonNode src = item.result().source();
                if (src == null) {
                    continue;
                }

                JsonNode emb = src.get("embedding");
                if (emb == null || !emb.isArray()) {
                    continue;
                }

                if (emb.size() != EMBEDDING_DIMS) {
                    continue;
                }

                float[] vec = new float[EMBEDDING_DIMS];
                for (int i = 0; i < EMBEDDING_DIMS; i++) {
                    vec[i] = (float) emb.get(i).asDouble();
                }

                vectors.add(vec);
            }

            return vectors;

        } catch (Exception e) {
            log.error("embedding 정보 조회 실패", e);
            return List.of();
        }
    }

    private long dailySeed(String userId) {
        String day = LocalDate.now(KST).toString();
        String key = (userId == null ? "anonymous" : userId) + "|" + day;

        CRC32 crc = new CRC32();
        crc.update(key.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }
}