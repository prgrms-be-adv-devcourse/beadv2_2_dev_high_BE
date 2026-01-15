package com.dev_high.search.infrastructure;

import com.dev_high.search.domain.ProductDocument;
import com.dev_high.search.domain.SearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SearchRepositoryAdapter implements SearchRepository {
    private final SearchElasticsearchRepository searchElasticsearchRepository;

    public SearchRepositoryAdapter(SearchElasticsearchRepository searchElasticsearchRepository) {
        this.searchElasticsearchRepository = searchElasticsearchRepository;
    }

    @Override
    public ProductDocument save(ProductDocument productDocument) {
        return searchElasticsearchRepository.save(productDocument);
    }

    @Override
    public Optional<ProductDocument> findByProductId(String productId) {
        return searchElasticsearchRepository.findById(productId);
    }

    @Override
    public void deleteByProductId(String productId) {
        searchElasticsearchRepository.deleteById(productId);
    }

    @Override
    public void saveAll(List<ProductDocument> docs) {
        searchElasticsearchRepository.saveAll(docs);
    }
}
