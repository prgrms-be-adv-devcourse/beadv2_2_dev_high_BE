package com.dev_high.product.presentation.dto;

import com.dev_high.product.application.dto.ProductCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "상품명은 필수입니다.") String name,
        String description,
        Long fileGroupId,
        @NotBlank(message = "판매자 ID는 필수입니다.") String sellerId,
        List<String> categoryIds
) {
    public ProductCommand toCommand() {
        return new ProductCommand(
                name,
                description,
                fileGroupId,
                sellerId,
                sellerId, // 생성자/수정자를 판매자 ID로 사용
                categoryIds
        );
    }
}
