package com.dev_high.search.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReaderConfig {
    private static final String INDEX = "product";
    private static final int SIZE = 200;

    @Bean
    public ProductReader fullReader(ElasticsearchClient esClient) {
        Query matchAll = Query.of(q -> q.matchAll(m -> m));
        return new ProductReader(
                esClient,
                INDEX,
                SIZE,
                matchAll
        );
    }

    @Bean
    public ProductReader missingReader(ElasticsearchClient esClient) {
        Query missing = Query.of(q -> q.bool(b -> b
                .must(m -> m.matchAll(ma -> ma))
                .mustNot(mn -> mn.exists(e -> e.field("embedding")))
        ));
        return new ProductReader(
                esClient,
                INDEX,
                SIZE,
                missing
        );
    }
}