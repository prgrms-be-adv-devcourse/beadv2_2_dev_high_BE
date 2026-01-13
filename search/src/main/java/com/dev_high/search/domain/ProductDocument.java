package com.dev_high.search.domain;

import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Document(indexName = "product", createIndex = false)
public class ProductDocument {

    @Id
    private String productId;

    private String auctionId;

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

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private Instant auctionStartAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private Instant auctionEndAt;

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] embedding;

    public ProductDocument() {}

    public ProductDocument(ProductCreateSearchRequestEvent request) {
        this.productId = request.productId();
        this.productName = request.productName();
        this.categories = request.categories();
        this.description = request.description();
        this.status = request.status();
        this.sellerId = request.sellerId();
    }

    public void updateByProduct(ProductUpdateSearchRequestEvent request) {
        this.productId = request.productId();
        this.productName = request.productName();
        this.categories = request.categories();
        this.description = request.description();
        this.sellerId = request.sellerId();
    }

    public void updateByAuction(AuctionUpdateSearchRequestEvent request) {
        this.auctionId = request.auctionId();
        this.startPrice = request.startPrice();
        this.depositAmount = request.depositAmount();
        this.status = request.status();
        this.auctionStartAt = request.auctionStartAt().toInstant();
        this.auctionEndAt = request.auctionEndAt().toInstant();
    }
}
