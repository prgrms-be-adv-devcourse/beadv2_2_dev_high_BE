package com.dev_high.product.presentation.dto;

import com.dev_high.product.application.dto.ProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "상품명은 필수입니다.") String name,
        String description,
        List<String> categoryIds,
        String fileGrpId,
        @NotNull(message = "경매 시작가는 필수입니다.") BigDecimal startBid,
        @NotBlank(message = "경매 시작 시간은 필수입니다.") String auctionStartAt,
        @NotBlank(message = "경매 종료 시간은 필수입니다.") String auctionEndAt
) {
    public ProductCommand toCommand() {
        return new ProductCommand(
                name,
                description,
                categoryIds,
                fileGrpId,
                startBid,
                auctionStartAt,
                auctionEndAt
        );
    }
}
