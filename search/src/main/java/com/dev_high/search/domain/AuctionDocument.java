package com.dev_high.search.domain;

import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(indexName = "auction", createIndex = false)
public class AuctionDocument {

    @Id
    private String auctionId;

    private String productId;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String productName;

    @Field(type = FieldType.Keyword)
    private List<String> categories;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 1)
    private BigDecimal  startPrice;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 1)
    private BigDecimal depositAmount;

    @Field(type = FieldType.Keyword)
    private String status;

    private String sellerId;

    private String redirectUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    private LocalDateTime auctionStartAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    private LocalDateTime auctionEndAt;


    public void updateAuction(AuctionUpdateSearchRequestEvent request) {
        this.productName = request.productName();
        this.categories = request.categories();
        this.description = request.description();
        this.startPrice = request.startPrice();
        this.depositAmount = request.depositAmount();
        this.status = request.status();
        this.auctionStartAt = request.auctionStartAt();
        this.auctionEndAt = request.auctionEndAt();
    }
}
