package com.dev_high.search.infrastructure;

import com.dev_high.search.domain.AuctionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface AuctionSearchRepository extends ElasticsearchRepository<AuctionDocument, String> {
    Optional<AuctionDocument> findByProductId(String productId);
    void deleteByProductId(String productId);
}