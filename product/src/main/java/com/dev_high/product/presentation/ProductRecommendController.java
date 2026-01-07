package com.dev_high.product.presentation;

import com.dev_high.product.application.ProductRecommendService;
import com.dev_high.product.application.dto.ProductRecommendResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/products/recommend")
@RequiredArgsConstructor
@Tag(name = "Products", description = "상품 임베딩/검색 API")
public class ProductRecommendController {

    private final ProductRecommendService productRecommendService;

    @PostMapping("/reindex")
    @Operation(summary = "상품 임베딩 재생성", description = "현재 DB에 있는 모든 상품을 임베딩하고 pgvector 컬럼을 갱신합니다.")
    public Map<String, Object> reindexAll() {
        int count = productRecommendService.indexAll();
        return Map.of("indexed", count);
    }

    @GetMapping("/search")
    @Operation(
            summary = "상품 벡터 검색",
            description = "질문을 임베딩하여 유사도가 높은 상품 상위 k개를 반환합니다. 상태=ACTIVE, 재고>0만 검색합니다.")
    public List<ProductRecommendResult> search(
            @RequestParam("q")
            @Parameter(description = "검색어(예: 무선 이어폰, 캠핑 체어)") String query,
            @RequestParam(value = "topK", defaultValue = "5")
            @Parameter(description = "가져올 상위 유사도 결과 개수 (기본 5)") int topK) {
        return productRecommendService.search(query, topK);
    }

    @GetMapping("/ask")
    @Operation(
            summary = "상품 RAG 답변",
            description = "질문을 임베딩→검색 후, 검색된 상품 정보를 컨텍스트로 GPT 답변을 생성합니다.")
    public productRecommendService.ProductAnswer ask(
            @RequestParam("q")
            @Parameter(description = "사용자 질문(예: 여름 캠핑용 의자 추천)") String query,
            @RequestParam(value = "topK", defaultValue = "5")
            @Parameter(description = "검색에 사용할 상위 결과 개수 (기본 5)") int topK) {
        return productRecommendService.answer(query, topK);
    }
}
