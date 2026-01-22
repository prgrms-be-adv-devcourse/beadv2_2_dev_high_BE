package com.dev_high.search.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.dev_high.search.domain.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ProductReader implements ItemStreamReader<ProductDocument> {
    private final ElasticsearchClient esClient;
    private final String index;
    private final int size;
    private final Query baseQuery;
    private String pitId;
    private List<FieldValue> searchAfter;
    private Iterator<ProductDocument> buffer = Collections.emptyIterator();
    private boolean finished = false;

    @Override
    public ProductDocument read() throws Exception {
        if (finished) {
            return null;
        }

        if (buffer != null && buffer.hasNext()) {
            return buffer.next();
        }

        List<ProductDocument> docs = fetchBatch();
        if (docs.isEmpty()) {
            finished = true;
            return null;
        }

        buffer = docs.iterator();
        return buffer.hasNext() ? buffer.next() : null;
    }

    private List<ProductDocument> fetchBatch() throws Exception {
        SearchResponse<ProductDocument> resp = esClient.search(s -> {
            s.size(size);
            s.pit(p -> p
                    .id(pitId)
                    .keepAlive(t -> t.time("5m"))
            );
            s.query(baseQuery);

            s.sort(so -> so.field(f -> f.field("_shard_doc").order(SortOrder.Asc)));

            if (searchAfter != null) {
                s.searchAfter(searchAfter);
            }
            return s;
        }, ProductDocument.class);

        List<Hit<ProductDocument>> hits = resp.hits().hits();
        if (hits == null || hits.isEmpty()) return List.of();

        searchAfter = hits.get(hits.size() - 1).sort();

        List<ProductDocument> out = new ArrayList<>(hits.size());
        for (Hit<ProductDocument> h : hits) {
            ProductDocument doc = h.source();
            if (doc == null) {
                continue;
            }

            if (doc.getProductId() == null || doc.getProductId().isBlank()) {
                doc.setProductId(h.id());
            }

            out.add(doc);
        }

        return out;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.finished = false;
        this.buffer = Collections.emptyIterator();
        this.searchAfter = null;

        try {
            this.pitId = esClient.openPointInTime(p -> p
                    .index(index)
                    .keepAlive(t -> t.time("5m"))
            ).id();

        } catch (Exception e) {
            throw new ItemStreamException("Elasticsearch PIT(Point In Time) 열기에 실패했습니다.", e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        if (pitId == null) {
            return;
        }

        try {
            esClient.closePointInTime(c -> c.id(pitId));
        } catch (Exception e) {
            throw new ItemStreamException("Elasticsearch PIT(Point In Time) 종료에 실패했습니다.", e);
        } finally {
            pitId = null;
        }
    }
}