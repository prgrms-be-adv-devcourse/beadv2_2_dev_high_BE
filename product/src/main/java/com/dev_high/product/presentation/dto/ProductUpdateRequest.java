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
        @JsonAlias("fileGrpId") String fileId,
        @NotNull(message = "경매 시작가는 필수입니다.") BigDecimal startBid,
        @NotBlank(message = "경매 시작 시간은 필수입니다.") String auctionStartAt,
        @NotBlank(message = "경매 종료 시간은 필수입니다.") String auctionEndAt
) {
    public ProductUpdateCommand toCommand() {
        return new ProductUpdateCommand(
                name,
                description,
                categoryIds,
                fileId,
                startBid,
                auctionStartAt,
                auctionEndAt
        );
    }
}
