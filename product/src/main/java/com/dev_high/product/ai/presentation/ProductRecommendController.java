package com.dev_high.product.ai.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.ai.application.ProductRecommendService;
import com.dev_high.product.ai.application.dto.ProductAnswer;
import com.dev_high.product.ai.application.dto.ProductSearchInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("${api.v1:/api/v1}/products/recommend")
@RequiredArgsConstructor
@Tag(name = "Products", description = "상품 임베딩/검색 API")
public class ProductRecommendController {


   private final ProductRecommendService productRecommendService;
//    @PostMapping("/index")
//    @Operation(summary = "상품단건 임베딩", description = "단일 상품을 임베딩합니다.")
//    public Map<String, Object> indexOne(String productId) {
//        int count = productRecommendService.indexOne(productId);
//        return Map.of("indexed", count);
//    }

    @PostMapping("/indexAll")
    @Operation(summary = "상품일괄 임베딩", description = "현재 DB에 있는 모든 상품을 임베딩합니다.")
    public ApiResponseDto<Map<String, Object>> indexAll() {

        int count = productRecommendService.indexAll();
        return ApiResponseDto.success(Map.of("indexed", count));
    }


    @GetMapping("/search")
    @Operation(
            summary = "상품 벡터 검색",
            description = "질문을 임베딩하여 유사도가 높은 상품 상위 k개를 반환합니다. 삭제되지 않은 상품만 검색합니다.")
    public ApiResponseDto<List<ProductSearchInfo>> search(
            @RequestParam("q")
            @Parameter(description = "검색어(예: 무선 이어폰, 캠핑 체어)") String query,
            @RequestParam(value = "topK", defaultValue = "5")
            @Parameter(description = "가져올 상위 유사도 결과 개수 (기본 5)") int topK) {

        List<ProductSearchInfo> result = productRecommendService.search(query, topK);
        return ApiResponseDto.success(result);
    }


    @GetMapping("/ask")
    @Operation(
            summary = "상품 RAG 답변",
            description = "임베딩/검색된 상품 정보를 컨텍스트로 GPT 답변을 생성합니다.")
    public ApiResponseDto<ProductAnswer> ask(
            @RequestParam("q")
            @Parameter(description = "사용자 질문(예: 여름 캠핑용 의자 추천)") String query,
            @RequestParam(value = "topK", defaultValue = "5")
            @Parameter(description = "검색에 사용할 상위 결과 개수 (기본 5)") int topK) {

        ProductAnswer answer= productRecommendService.answer(query, topK);


        return ApiResponseDto.success(answer);
    }
}
