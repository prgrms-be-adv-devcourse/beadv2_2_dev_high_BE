package com.dev_high.search.application;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.event.auction.*;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.search.application.dto.ProductSearchResponse;
import com.dev_high.common.dto.SimilarProductResponse;
import com.dev_high.search.domain.ProductDocument;
import com.dev_high.search.domain.SearchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.*;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;

@AllArgsConstructor
@Service
@Slf4j
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchRepository searchRepository;
    private final EmbeddingModel embeddingModel;
    private final ElasticsearchClient elasticsearchClient;

    public void indexProduct(ProductCreateSearchRequestEvent request) {
        ProductDocument document = new ProductDocument(request);

        String text = buildEmbeddingText(document);
        float[] embedding = embeddingModel.embed(text);
        document.setEmbedding(embedding);

        searchRepository.save(document);
    }

    public void updateByProduct(ProductUpdateSearchRequestEvent request) {
        ProductDocument document = searchRepository.findByProductId(request.productId()).orElseThrow(RuntimeException::new);
        document.updateByProduct(request);

        String text = buildEmbeddingText(document);
        float[] embedding = embeddingModel.embed(text);
        document.setEmbedding(embedding);

        searchRepository.save(document);
    }

    public void updateByAuction(AuctionUpdateSearchRequestEvent request) {
        ProductDocument document = searchRepository.findByProductId(request.productId()).orElseThrow(RuntimeException::new);
        document.updateByAuction(request);
        searchRepository.save(document);
    }

    public void deleteByProduct(String productId) {
        searchRepository.deleteByProductId(productId);
    }

    public ApiResponseDto<Page<ProductSearchResponse>> searchProducts(
            String keyword,
            List<String> categories,
            String status,
            BigDecimal minStartPrice,
            BigDecimal maxStartPrice,
            String startFrom,
            String startTo,
            Pageable pageable) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            boolQuery.should(m -> m.matchPhrase(mm -> mm
                    .field("productName")
                    .query(keyword)
                    .slop(1)
                    .boost(2.0f)
            ));
            boolQuery.should(m -> m.match(mm -> mm
                    .field("productName")
                    .query(keyword)
                    .operator(Operator.Or)
                    .boost(1.0f)
            ));
            boolQuery.should(m -> m.match(mm -> mm
                    .field("description")
                    .query(keyword)
                    .operator(Operator.Or)
                    .boost(1.0f)
            ));
            boolQuery.minimumShouldMatch("1");
        }

        // 카테고리 필터
        if (categories != null && !categories.isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("categories")
                    .terms(tv -> tv.value(categories.stream().map(FieldValue::of).toList()))
            ));
        }

        // 상태 필터
        if (status != null && !status.isBlank()) {
            boolQuery.filter(f -> f.term(t -> t.field("status").value(status)));
        }

        // startPrice 범위
        if (minStartPrice != null || maxStartPrice != null) {
            boolQuery.filter(f -> f.bool(b -> {
                b.should(s -> s.range(r -> r.number(n -> {
                    n.field("startPrice");
                    if (minStartPrice != null) n.gte(minStartPrice.doubleValue());
                    if (maxStartPrice != null) n.lte(maxStartPrice.doubleValue());
                    return n;
                })));
                b.should(s -> s.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field("startPrice")))));
                b.minimumShouldMatch("1");
                return b;
            }));
        }

        if (startFrom != null || startTo != null) {
            Instant startFromInstant = parseKstToUtc(startFrom);
            Instant startToInstant = parseKstToUtc(startTo);

            boolQuery.filter(f -> f.bool(b -> {
                b.should(s -> s.range(r -> r.date(d -> {
                    d.field("auctionStartAt");
                    if (startFrom != null) d.gte(startFromInstant.toString());
                    if (startTo != null) d.lte(startToInstant.toString());
                    return d;
                })));
                b.should(s -> s.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field("auctionStartAt")))));
                b.minimumShouldMatch("1");
                return b;
            }));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .withSort(s -> s.field(f -> f
                        .field("auctionStartAt")
                        .order(SortOrder.Desc)
                        .missing("_last")
                ))
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        SearchPage<ProductDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);

        return ApiResponseDto.success(searchPage.map(hit -> ProductSearchResponse.from(hit.getContent())));
    }

    public List<SimilarProductResponse> findSimilarProducts(String productId, int limit) {
        if (productId == null || productId.isBlank() || limit <= 0) {
            return List.of();
        }

        ProductDocument base = searchRepository.findByProductId(productId)
            .orElse(null);
        if (base == null) {
            return List.of();
        }

        float[] embedding = base.getEmbedding();
        if (embedding == null || embedding.length == 0) {
            String text = buildEmbeddingText(base);
            embedding = embeddingModel.embed(text);
        }

        List<Float> queryVector = new ArrayList<>(embedding.length);
        for (float v : embedding) {
            queryVector.add(v);
        }

        int k = limit + 1;
        int numCandidates = Math.max(100, k * 5);

        SearchResponse<ProductDocument> response;
        try {
            response = elasticsearchClient.search(s -> s
                    .index("product")
                    .knn(kq -> kq
                            .field("embedding")
                            .queryVector(queryVector)
                            .k(k)
                            .numCandidates(numCandidates)
                    )
                    .source(src -> src.filter(f -> f.includes("productId")))
            , ProductDocument.class);
        } catch (Exception e) {
            log.warn("similar search failed: {}", e.getMessage());
            return List.of();
        }

        return response.hits().hits().stream()
            .filter(hit -> hit.source() != null)
            .filter(hit -> !productId.equals(hit.source().getProductId()))
            .limit(limit)
            .map(hit -> new SimilarProductResponse(
                hit.source().getProductId(),
                hit.score() == null ? 0.0 : hit.score()
            ))
            .toList();
    }

    private Instant parseKstToUtc(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);

        return localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    private String buildEmbeddingText(ProductDocument document) {

        String categories = document.getCategories() != null
                ? String.join(", ", document.getCategories())
                : "";

        return "%s %s %s".formatted(
                document.getProductName(),
                document.getDescription() != null ? document.getDescription() : "",
                categories
        );
    }
}
