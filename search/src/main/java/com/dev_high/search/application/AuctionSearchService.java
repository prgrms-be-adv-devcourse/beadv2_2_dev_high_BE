package com.dev_high.search.application;


import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.event.product.*;
import com.dev_high.common.kafka.event.auction.*;
import com.dev_high.search.domain.AuctionDocument;
import com.dev_high.search.infrastructure.AuctionSearchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;

import java.util.List;


@AllArgsConstructor
@Service
@Slf4j
public class AuctionSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AuctionSearchRepository auctionSearchRepository;

    public void indexAuction(AuctionCreateSearchRequestEvent request) {
        AuctionDocument document = AuctionDocument.builder()
                .auctionId(request.auctionId())
                .productId(request.productId())
                .productName(request.productName())
                .categories(request.categories())
                .description(request.description())
                .startPrice(request.startPrice())
                .depositAmount(request.depositAmount())
                .status(request.status())
                .auctionStartAt(request.auctionStartAt())
                .auctionEndAt(request.auctionEndAt())
                .sellerId(request.sellerId())
                .redirectUrl("/" + request.productId())
                .build();
        auctionSearchRepository.save(document);
    }

    public void deleteByProduct(String productId) {
        auctionSearchRepository.deleteByProductId(productId);
    }

    public void deleteByAuction(String auctionId) {
        auctionSearchRepository.deleteByProductId(auctionId);
    }

    public void updateByProduct(ProductUpdateSearchRequestEvent request) {
        AuctionDocument document = auctionSearchRepository.findByProductId(request.productId()).orElseThrow(RuntimeException::new);
        document.updateProduct(request);
        auctionSearchRepository.save(document);
    }

    public void updateByAuction(AuctionUpdateSearchRequestEvent request) {
        AuctionDocument document = auctionSearchRepository.findByProductId(request.auctionId()).orElseThrow(RuntimeException::new);
        document.updateAuction(request);
       auctionSearchRepository.save(document);
    }

    public ApiResponseDto<Page<AuctionDocument>> searchAuctions(String keyword, List<String> categories, String status, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    // 키워드 검색
                    if (keyword != null && !keyword.isBlank()) {
                        // 순서 그대로 or 단어 사이에 최대 한 단어 허용
                        b.should(m -> m.matchPhrase(mm -> mm
                                .field("productName")
                                .query(keyword)
                                .slop(1)
                                .boost(2.0f)
                        ));

                        //단어 중 하나만 있어도 매칭
                        b.should(m -> m.match(mm -> mm
                                .field("productName")
                                .query(keyword)
                                .operator(Operator.Or)
                                .boost(1.0f)
                        ));

                        b.should(m -> m.match(mm -> mm
                                .field("description")
                                .query(keyword)
                                .operator(Operator.Or)
                                .boost(1.0f)
                        ));

                        b.minimumShouldMatch("1");
                    }

                    // 카테고리 필터
                    if (categories != null && !categories.isEmpty()) {
                        b.filter(f -> f.terms(t -> t
                                .field("categories")
                                .terms(tv -> tv.value(
                                        categories.stream()
                                                .map(FieldValue::of)
                                                .toList()
                                ))
                        ));
                    }

                    // 상태 필터
                    if (status != null && !status.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("status").value(status)));
                    }

                    return b;
                }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc))) // 점수순
                .withSort(s -> s.field(f -> f.field("auctionStartAt").order(SortOrder.Desc))) // 최신순
                .withPageable(pageable)
                .build();

        SearchHits<AuctionDocument> hits = elasticsearchOperations.search(query, AuctionDocument.class);
        SearchPage<AuctionDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);
        return ApiResponseDto.success(searchPage.map(SearchHit::getContent));
    }
}
