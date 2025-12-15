package com.dev_high.search.config;

import com.dev_high.search.domain.AuctionDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void createAuctionIndexIfNotExists() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(AuctionDocument.class);

        if (!indexOps.exists()) {
            log.info("Elasticsearch 인덱스가 존재하지 않아 새로 생성합니다.");

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

            indexOps.create(settings);
            indexOps.putMapping(indexOps.createMapping());
            log.info("Elasticsearch 인덱스 생성이 완료되었습니다.");
        } else {
            log.info("Elasticsearch 인덱스가 이미 존재합니다.");
        }
    }
}