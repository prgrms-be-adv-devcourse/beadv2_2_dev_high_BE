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
import com.dev_high.search.application.mapper.ProductRecommendMapper;
import com.dev_high.search.util.VectorUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private static final String INDEX = "product";
    private static final int EMBEDDING_DIMS = 1536;
    private static final int LIMIT = 10;
    private static final int NUM_CANDIDATES_FACTOR = 20;

    private final ElasticsearchClient elasticsearchClient;
    private final AiRecommendationSummaryGenerator summaryGenerator;

    public List<ProductRecommendResponse> recommendByWishlist(List<String> wishlistProductIds) {
        String userId = UserContext.get().userId();
        
        if (wishlistProductIds == null || wishlistProductIds.isEmpty()) {
            return fallback(wishlistProductIds, userId);
        }

        List<float[]> vectors = fetchEmbeddings(wishlistProductIds);
        if (vectors.isEmpty()) {
            return fallback(wishlistProductIds, userId);
        }

        float[] userVector = VectorUtils.meanVector(vectors);
        VectorUtils.l2NormalizeInPlace(userVector);

        List<ProductRecommendResponse> candidates =
                searchCandidatesByKnn(userVector, wishlistProductIds, userId);

        if (!candidates.isEmpty()) {
            return candidates;
        }

        return fallback(wishlistProductIds, userId);
    }

    public ApiResponseDto<ProductRecommendSummaryResponse> recommendByWishlistWithSummary(List<String> wishlistProductIds) {
        List<ProductRecommendResponse> items = recommendByWishlist(wishlistProductIds);

        String summary = summaryGenerator.summarize(items);

        return ApiResponseDto.success(new ProductRecommendSummaryResponse(summary, items));
    }

    private List<ProductRecommendResponse> searchCandidatesByKnn(
            float[] userVector,
            List<String> wishlistIds,
            String userId
    ) {
        int k = LIMIT;
        int numCandidates = Math.min(1000, Math.max(200, k * NUM_CANDIDATES_FACTOR));

        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(INDEX)
                    .size(k)
                    .knn(knn -> knn
                            .field("embedding")
                            .queryVector(VectorUtils.toFloatList(userVector))
                            .k(k)
                            .numCandidates(numCandidates)
                    )
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
            throw new RuntimeException("kNN 추천 검색 실패", e);
        }
    }

    private List<ProductRecommendResponse> fallback (
            List<String> wishlistIds,
            String userId
    ) {
        return fallbackEsOnly(wishlistIds, userId);
    }

    private List<ProductRecommendResponse> fallbackEsOnly (
            List<String> wishlistIds,
            String userId
    ) {
        int k = LIMIT;

        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(INDEX)
                    .size(k)
                    .query(q -> q.bool(b -> {
                        b.must(m -> m.matchAll(ma -> ma));
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
            log.warn("기본 검색(fallback) 수행 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * - wishlist 원본 productId 제외
     * - sellerId == userId 제외
     * - status == COMPLETED 제외
     */
    private void applyRecommendFilters(
            BoolQuery.Builder b,
            List<String> excludeProductIds,
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

    private List<float[]> fetchEmbeddings(List<String> ids) {
        try {
            MgetRequest mget = MgetRequest.of(m -> m.index(INDEX).ids(ids));
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

                ArrayNode arr = (ArrayNode) emb;
                if (arr.size() != EMBEDDING_DIMS) {
                    continue;
                }

                float[] vec = new float[EMBEDDING_DIMS];
                for (int i = 0; i < EMBEDDING_DIMS; i++) {
                    vec[i] = (float) arr.get(i).asDouble();
                }
                vectors.add(vec);
            }

            return vectors;

        } catch (Exception e) {
            log.error("embedding 정보 조회 실패", e);
            throw new RuntimeException("embedding 정보 조회 실패", e);
        }
    }
}
