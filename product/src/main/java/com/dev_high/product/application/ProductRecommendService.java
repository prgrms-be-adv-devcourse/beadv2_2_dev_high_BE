package com.dev_high.product.application;

import com.dev_high.product.application.dto.DraftTonePreset;
import com.dev_high.product.application.dto.ProductAnswer;
import com.dev_high.product.application.dto.ProductDetailDto;
import com.dev_high.product.application.dto.ProductRecommendationToolResponse;
import com.dev_high.product.application.dto.ProductSearchInfo;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private final ProductRecommendationTool productRecommendationTool;
    private final ObjectMapper objectMapper;
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
                                "deletedYn == 'N'"
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

        String systemText = """
                너는 중고 경매 플랫폼의 상품 추천 도우미다.
                아래 규칙을 반드시 지켜라.
                1) 반드시 tool "product_recommendation_response"를 호출하여 응답한다.
                2) intent는 GREETING, PRODUCT, SERVICE, OFF_TOPIC, ABUSIVE 중 하나만 사용한다.
                3) PRODUCT인 경우 제공된 Context 범위 안에서만 답한다.
                4) PRODUCT인데 Context가 비어 있으면 추천 가능한 상품이 없음을 안내하고 조건을 요청한다.
                5) GREETING은 짧게 인사 후 상품/조건 질문을 유도한다.
                6) SERVICE는 로그인/결제/배송 등 문의에 간단히 답하고 필요한 정보를 요청한다. 확실치 않은 내용은 단정하지 않는다.
                7) OFF_TOPIC은 부드럽게 주제를 전환한다.
                8) ABUSIVE는 정중히 거절한다.
                9) 답변은 한국어 1~3문장으로 작성한다.
                """;

        String userText = """
                질문: %s
                Context:
                %s
                Context 상태: %s
                """.formatted(query, context, context.isBlank() ? "EMPTY" : "FILLED");

        ChatResponse response = chatClient.prompt()
                .system(systemText)
                .user(userText)
                .tools(productRecommendationTool)
                .call()
                .chatResponse();
        log.info("chat response raw: {}", response);

        ProductRecommendationToolResponse toolResponse = parseToolResponse(response);
        if (toolResponse == null || !StringUtils.hasText(toolResponse.answer())) {
            return new ProductAnswer("현재 추천 답변을 생성하기 어려워요. 찾는 상품이나 조건을 조금 더 알려주세요.", List.of());
        }
        if (isProductIntent(toolResponse.intent())) {
            return new ProductAnswer(toolResponse.answer(), productSearchInfo);
        }
        return new ProductAnswer(toolResponse.answer(), List.of());
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

    private boolean isProductIntent(String intent) {
        if (intent == null) {
            return false;
        }
        return "PRODUCT".equalsIgnoreCase(intent.trim());
    }

    private ProductRecommendationToolResponse parseToolResponse(ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return null;
        }
        String content = response.getResult().getOutput().getText();
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String json = extractJson(content);
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ProductRecommendationToolResponse.class);
        } catch (Exception e) {
            log.warn("tool response parse failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return null;
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
                product.getId(),
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
