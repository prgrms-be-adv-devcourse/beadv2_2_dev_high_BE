package com.dev_high.product.application;

import com.dev_high.product.application.ai.dto.ProductAnswer;
import com.dev_high.product.application.dto.ProductSearchInfo;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.domain.ai.ChatMessage;
import com.dev_high.product.domain.ai.ChatModel;
import com.dev_high.product.domain.ai.ChatResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRecommendService {

    private final EmbeddingModel embeddingModel;
    private final ProductRepository productRepository;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;


    //단건색인
    public void indexOne(Product product) {
        Document newDoc=toDocument(product);
        vectorStore.add(List.of(newDoc));
    }


    //일괄색인
    public int indexAll() {

        List<Product> products= productRepository.findAll();

        List<Document> docList=products.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(docList);

        return docList.size();
    }

    // 상품 추천용 검색
    public List<ProductSearchInfo> search(String query, int topK) {

        List<Document> documents=vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .filterExpression(
                                "deletedYn == 'Y'"
                        )
                        .build()
        );

        if(documents==null || documents.isEmpty()){
            return List.of();
        }

        return documents.stream()
                .map(ProductSearchInfo::from).toList();
    }


    //재색인
    public void reindex(Product product) {
        vectorStore.delete(List.of(product.getId()));
        vectorStore.add(List.of(toDocument(product)));

    }


    // 상품RAG 추천
    public ProductAnswer answer(String query, int topK) {

        List<ProductSearchInfo> productSearchInfo= search(query, topK);

        String context = toContext(productSearchInfo);
        ChatResult result = chatModel.chat(new ChatMessage(query, context));


        ProductAnswer answer=new ProductAnswer(result.content(), productSearchInfo);

        return answer;
    }


    private String toContext(List<ProductSearchInfo> productSearchInfo) {
        if (productSearchInfo == null || productSearchInfo.isEmpty()) {
            return "";
        }

        return productSearchInfo.stream()
                .map(info -> String.format(
                        "productId=%s | name=%s | description=%s | categories=%s | sellerId=%s",
                        nullToEmpty(info.productId()),
                        nullToEmpty(info.name()),
                        nullToEmpty(info.description()),
                        nullToEmpty(info.categories()),
                        nullToEmpty(info.sellerId())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }


    private Document toDocument(Product product) {
        String categories = product.getCategories().stream()
                .map(Category::getCategoryName)
                .distinct()
                .collect(Collectors.joining(", "));

        // document.content 생성
        String content = """
        상품명: %s
        상품 설명: %s
        카테고리: %s
        판매자: %s
        """.formatted(
                product.getName(),
                product.getDescription() != null ? product.getDescription() : "",
                categories,
                product.getSellerId()
        );

        return new Document(
                content,
                Map.of(
                        "productId", product.getId(),
                        "name", product.getName(),
                        "description", product.getDescription() != null ? product.getDescription() : "",
                        "sellerId", product.getSellerId(),
                        "categories", categories,
                        "deletedYn", product.getDeletedYn().name()
                )
        );
    }

}
