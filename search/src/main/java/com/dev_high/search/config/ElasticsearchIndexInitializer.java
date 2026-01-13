package com.dev_high.search.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final String INDEX_NAME = "product";

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            createProductIndexIfNotExists();
        } catch (Exception e) {
            log.error("Elasticsearch 인덱스 초기화 실패", e);
        }
    }

    private void createProductIndexIfNotExists() {

        IndexOperations indexOps =
                elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_NAME));

        if (indexOps.exists()) {
            return;
        }

        Map<String, Object> settings = Map.of(
                "analysis", Map.of(
                        "analyzer", Map.of(
                                "nori_analyzer", Map.of(
                                        "type", "custom",
                                        "tokenizer", "nori_tokenizer"
                                )
                        )
                )
        );

        Map<String, Object> mappings = Map.of(
                "properties", Map.ofEntries(
                        Map.entry("productId", Map.of("type", "keyword")),
                        Map.entry("auctionId", Map.of("type", "keyword")),
                        Map.entry("sellerId", Map.of("type", "keyword")),

                        Map.entry("productName", Map.of(
                                "type", "text",
                                "analyzer", "nori_analyzer"
                        )),
                        Map.entry("description", Map.of(
                                "type", "text",
                                "analyzer", "nori_analyzer"
                        )),

                        Map.entry("categories", Map.of("type", "keyword")),
                        Map.entry("status", Map.of("type", "keyword")),
                        Map.entry("imageUrl", Map.of("type", "keyword")),

                        Map.entry("startPrice", Map.of(
                                "type", "scaled_float",
                                "scaling_factor", 1
                        )),
                        Map.entry("depositAmount", Map.of(
                                "type", "scaled_float",
                                "scaling_factor", 1
                        )),

                        Map.entry("auctionStartAt", Map.of("type", "date")),
                        Map.entry("auctionEndAt", Map.of("type", "date")),

                        Map.entry("embedding", Map.of(
                                "type", "dense_vector",
                                "dims", 1536
                        ))
                )
        );

        indexOps.create(settings);
        indexOps.putMapping(Document.from(mappings));
    }
}
