package com.dev_high.search.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.dev_high.search.domain.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductWriter implements ItemWriter<ProductDocument> {

    private final ElasticsearchClient esClient;
    private static final String INDEX = "product";

    @Override
    public void write(Chunk<? extends ProductDocument> chunk) throws Exception {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        esClient.bulk(b -> {
            for (ProductDocument doc : chunk.getItems()) {
                if (doc == null) continue;

                String id = doc.getProductId();
                float[] embedding = doc.getEmbedding();

                if (id == null || id.isBlank()) {
                    continue;
                }
                if (embedding == null || embedding.length == 0) {
                    continue;
                }

                b.operations(op -> op.update(u -> u
                        .index(INDEX)
                        .id(id)
                        .action(a -> a
                                .doc(new EmbeddingPatch(embedding))
                                .docAsUpsert(false)
                        )
                ));
            }
            return b;
        });
    }

    public record EmbeddingPatch(float[] embedding) {}
}
