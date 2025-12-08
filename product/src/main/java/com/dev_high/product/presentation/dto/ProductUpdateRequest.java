package com.dev_high.product.presentation.dto;

import com.dev_high.product.application.dto.ProductUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProductUpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.") String name,
        String description,
        Long fileGroupId,
        @NotBlank(message = "판매자 ID는 필수입니다.") String sellerId,
        List<String> categoryIds
) {
    public ProductUpdateCommand toCommand() {
        return new ProductUpdateCommand(
                name,
                description,
                fileGroupId,
                sellerId,
                categoryIds
        );
    }
}
