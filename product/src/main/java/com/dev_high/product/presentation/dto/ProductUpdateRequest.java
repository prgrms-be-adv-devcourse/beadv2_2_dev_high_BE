package com.dev_high.product.presentation.dto;

import com.dev_high.product.application.dto.ProductUpdateCommand;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.") String name,
        String description,
        List<String> categoryIds,
        @JsonAlias("fileGrpId") String fileId
) {
    public ProductUpdateCommand toCommand() {
        return new ProductUpdateCommand(
                name,
                description,
                categoryIds,
                fileId
        );
    }
}
