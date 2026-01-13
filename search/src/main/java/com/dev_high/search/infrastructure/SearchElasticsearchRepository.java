package com.dev_high.search.infrastructure;

import com.dev_high.search.domain.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
}