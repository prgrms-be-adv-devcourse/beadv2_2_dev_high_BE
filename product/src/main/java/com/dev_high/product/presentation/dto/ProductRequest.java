package com.dev_high.product.presentation.dto;

import com.dev_high.product.application.dto.ProductCommand;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "상품명은 필수입니다.") String name,
        String description,
        List<String> categoryIds,
        @JsonAlias("fileGrpId") String fileId,
        String fileURL
) {
    public ProductCommand toCommand() {
        return new ProductCommand(
                name,
                description,
                categoryIds,
                fileId,
                fileURL
        );
    }
}
