package com.dev_high.product.application;

import com.dev_high.common.dto.ChatResult;
import com.dev_high.product.application.dto.DraftTonePreset;
import com.dev_high.product.application.dto.ProductAnswer;
import com.dev_high.product.application.dto.ProductDetailDto;
import com.dev_high.product.application.dto.ProductSearchInfo;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductRecommendService {

    private final ProductRepository productRepository;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final PromptTemplate recommendTemplate;
    private final PromptTemplate imageToDetailWithCategoryTemplate;


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

    //재색인
    public void reindex(Product product) {
        vectorStore.delete(List.of(product.getId()));
        vectorStore.add(List.of(toDocument(product)));

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

    // 상품RAG 추천
    public ProductAnswer answer(String query, int topK) {

        List<ProductSearchInfo> productSearchInfo= search(query, topK);
        String context = toContext(productSearchInfo);

        Prompt prompt = recommendTemplate.create(Map.of(
                "question", query,
                "context", context
        ));

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        log.info("chat response raw: {}", response);

        Map<String, Object> metadata = new HashMap<>();
        response.getMetadata().entrySet()
                .forEach(entry -> metadata.put(entry.getKey(), entry.getValue()));

        String content = response.getResult().getOutput().getText();
        log.info("chat response: content='{}', metadata={}", content, metadata);

        ChatResult<String> result = new ChatResult<>(content, metadata);

        return new ProductAnswer(result.content(), productSearchInfo);
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


    //상품상세설명 추천
    public ProductDetailDto chatProductDetail(
            MultipartFile[] files,
            String categoryOptions,   // ✅ 호출자가 만들어서 넘김
            Integer retryCount
    ) {


        String systemText = imageToDetailWithCategoryTemplate.render(Map.of(
                "categoryOptions", categoryOptions
        ));

        String addContext = DraftTonePreset.buildExtraContext(retryCount);
        String userText = "첨부된 여러 장의 이미지를 종합해서, 위 필드 목록을 만족하는 JSON 한 개 객체만 출력해줘.\n추가 컨텍스트: " + addContext;


        try {
            return callEntity(systemText, userText, files);
        } catch (Exception e) {
            // 1회 재시도
            String retryText = userText + "\n\n반드시 유효한 JSON 한 개 객체만 다시 출력해줘. 문자열 따옴표/괄호를 빠뜨리지 마.";
            return callEntity(systemText, retryText, files);
        }
    }

    private ProductDetailDto callEntity(String systemText, String userText, MultipartFile[] files) {
        return chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(systemText)
                .user(u -> {
                    u.text(userText);
                    for (MultipartFile f : files) {
                        validateImage(f);
                        String mime = (f.getContentType() == null || f.getContentType().isBlank()) ? "image/jpeg" : f.getContentType();
                        u.media(MimeTypeUtils.parseMimeType(mime), f.getResource());
                    }
                })
                .call()
                .entity(ProductDetailDto.class);
    }


    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is empty");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("only image/* allowed. contentType=" + ct);
        }
    }


}
