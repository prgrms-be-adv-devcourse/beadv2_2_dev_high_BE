package com.dev_high.search.application;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.event.auction.*;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.search.application.dto.ProductAutocompleteResponse;
import com.dev_high.search.application.dto.ProductSearchResponse;
import com.dev_high.common.dto.SimilarProductResponse;
import com.dev_high.search.domain.ProductDocument;
import com.dev_high.search.domain.SearchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.*;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
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
            OffsetDateTime startFrom,
            OffsetDateTime startTo,
            Pageable pageable
    ) {
        String kw = keyword == null ? "" : keyword.trim();
        int len = kw.replaceAll("\\s+", "").length();
        boolean hasKeyword = !kw.isBlank() && len > 1;

        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (categories != null && !categories.isEmpty()) {
            bool.filter(f -> f.terms(t -> t.field("categories")
                    .terms(v -> v.value(categories.stream().map(FieldValue::of).toList()))));
        }

        if (status != null && !status.isBlank()) {
            bool.filter(f -> f.term(t -> t.field("status").value(status)));
        }

        if (minStartPrice != null || maxStartPrice != null) {
            bool.filter(f -> f.bool(b -> b
                    .should(s -> s.range(r -> r.number(n -> {
                        n.field("startPrice");
                        if (minStartPrice != null) n.gte(minStartPrice.doubleValue());
                        if (maxStartPrice != null) n.lte(maxStartPrice.doubleValue());
                        return n;
                    })))
                    .should(s -> s.bool(bb -> bb.mustNot(m -> m.exists(e -> e.field("startPrice")))))
                    .minimumShouldMatch("1")
            ));
        }

        if (startFrom != null || startTo != null) {
            bool.filter(f -> f.bool(b -> b
                    .should(s -> s.range(r -> r.date(d -> {
                        d.field("auctionStartAt");
                        if (startFrom != null) d.gte(startFrom.toString());
                        if (startTo != null) d.lte(startTo.toString());
                        return d;
                    })))
                    .should(s -> s.bool(bb -> bb.mustNot(m -> m.exists(e -> e.field("auctionStartAt")))))
                    .minimumShouldMatch("1")
            ));
        }

        if (hasKeyword) {
            String qNoSpace = kw.replaceAll("\\s+", "");

            bool.must(m -> m.bool(b -> {
                b.should(s -> s.matchPhrase(mp -> mp.field("productName").query(kw).slop(1).boost(4f)));
                b.should(s -> s.match(mp -> mp.field("productName").query(kw).operator(Operator.And).boost(3f)));

                if (len >= 3) {
                    b.should(s -> s.match(mp -> mp.field("productName")
                            .query(kw)
                            .operator(Operator.Or)
                            .minimumShouldMatch("70%")
                            .fuzziness("AUTO")
                            .prefixLength(1)
                            .maxExpansions(50)
                            .boost(0.4f)));
                }

                b.should(s -> s.match(mp -> mp.field("description").query(kw).boost(0.2f)));

                if (len >= 3) {
                    b.should(s -> s.match(mp -> mp.field("productName.ngram")
                            .query(qNoSpace)
                            .minimumShouldMatch(len <= 4 ? "1" : len <= 7 ? "40%" : "60%")
                            .boost(0.05f)));
                }

                return b.minimumShouldMatch("1");
            }));
        } else {
            bool.must(m -> m.matchAll(ma -> ma));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(bool.build()))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withPageable(pageable)
                .withSourceFilter(new FetchSourceFilter(true, new String[]{}, new String[]{"embedding"}))
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        Page<ProductSearchResponse> page = new PageImpl<>(
                hits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .map(ProductSearchResponse::from)
                        .toList(),
                pageable,
                hits.getTotalHits()
        );

        return ApiResponseDto.success("상품 검색이 완료되었습니다.", page);
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
                            .source(src -> src.filter(f -> f.includes("productId", "auctionId", "imageUrl")))
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
                        hit.source().getAuctionId(),
                        hit.source().getImageUrl(),
                        hit.score() == null ? 0.0 : hit.score()
                ))
                .toList();
    }

    public ApiResponseDto<ProductAutocompleteResponse> autocompleteProductName(String keyword, int size) {
        String k = keyword == null ? "" : keyword.trim();
        if (k.isBlank()) {
            return ApiResponseDto.success(
                    "검색어 자동완성이 완료되었습니다.",
                    ProductAutocompleteResponse.of(List.of())
            );
        }

        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.multiMatch(mm -> mm
                            .query(k)
                            .type(TextQueryType.BoolPrefix)
                            .fields(
                                    "productNameSayt",
                                    "productNameSayt._2gram",
                                    "productNameSayt._3gram"
                            )
                    ))
                    .withSourceFilter(
                            new FetchSourceFilter(
                                    true,
                                    new String[]{},
                                    new String[]{"embedding"}
                            )
                    )
                    .withPageable(PageRequest.of(0, size))
                    .build();

            List<String> result = elasticsearchOperations
                    .search(query, ProductDocument.class)
                    .getSearchHits()
                    .stream()
                    .map(SearchHit::getContent)
                    .map(ProductDocument::getProductName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            return ApiResponseDto.success(
                    "검색어 자동완성이 완료되었습니다.",
                    ProductAutocompleteResponse.of(result)
            );

        } catch (Exception e) {
            log.warn("자동완성 조회 실패: prefix={}, size={}, err={}", k, size, e.toString());
            return ApiResponseDto.success(
                    "검색어 자동완성에 실패했습니다.",
                    ProductAutocompleteResponse.of(List.of())
            );
        }
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

    public void backfillEmbeddingsForAllProducts() {
        final int BATCH_SIZE = 200;
        int page = 0;
        long updated = 0L;

        while (true) {

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.matchAll(m -> m))
                    .withPageable(org.springframework.data.domain.PageRequest.of(page, BATCH_SIZE))
                    .build();

            SearchHits<ProductDocument> hits =
                    elasticsearchOperations.search(query, ProductDocument.class);

            if (hits.isEmpty()) {
                break;
            }

            List<ProductDocument> docs = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .toList();

            for (ProductDocument doc : docs) {
                String text = buildEmbeddingText(doc);

                if (text.isBlank()) {
                    continue;
                }

                float[] embedding = embeddingModel.embed(text);
                doc.setEmbedding(embedding);
            }

            searchRepository.saveAll(docs);
            updated += docs.size();

            log.info(
                    "[EMBEDDING BACKFILL] page={}, batchSize={}, totalUpdated={}",
                    page, docs.size(), updated
            );

            page++;
        }
    }
}