package com.dev_high.product.presentation.dto;

import com.dev_high.product.application.dto.ProductCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    private String description;

    private Long fileGroupId;

    @NotBlank(message = "판매자 ID는 필수입니다.")
    private String sellerId;

    @NotBlank(message = "생성자 ID는 필수입니다.")
    private String createdBy;

    public ProductCommand toCommand() {
        return ProductCommand.builder()
                .name(name)
                .description(description)
                .fileGroupId(fileGroupId)
                .sellerId(sellerId)
                .createdBy(createdBy)
                .build();
    }
}
