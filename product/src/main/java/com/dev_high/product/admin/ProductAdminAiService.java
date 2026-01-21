package com.dev_high.product.admin;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.exception.CustomException;
import com.dev_high.product.admin.dto.AiProductGenerateRequest;
import com.dev_high.product.admin.dto.AiProductSpec;
import com.dev_high.product.admin.dto.AiProductSpecList;
import com.dev_high.product.admin.dto.ByteArrayMultipartFile;
import com.dev_high.product.admin.dto.ImagePromptResponse;
import com.dev_high.product.application.FileService;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.CategoryRepository;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAdminAiService {

    private final CategoryRepository categoryRepository;
    private final ChatClient chatClient;
    private final PromptTemplate aiProductGenerateTemplate;
    private final PromptTemplate aiProductImagePromptTemplate;
    private final ImageModel imageModel;
    private final ProductAiTool productAiTool;
    private final FileService fileService;
    private final ProductAdminService productService;

    @Async
    public CompletableFuture<List<ProductInfo>> generateAiProductsAsync(
        AiProductGenerateRequest request,
        UserContext.UserInfo userInfo
    ) {
        UserContext.set(userInfo);
        try {
            return CompletableFuture.completedFuture(generateAiProducts(request));
        } finally {
            UserContext.clear();
        }
    }

    private List<ProductInfo> generateAiProducts(AiProductGenerateRequest request) {
        if (request == null || request.categories() == null || request.categories().isEmpty()) {
            throw new CustomException("카테고리와 생성 개수가 필요합니다.");
        }

        Map<String, String> categoryMap = categoryRepository.findAll().stream()
            .sorted(Comparator.comparing(Category::getId))
            .collect(HashMap::new, (m, c) -> m.put(c.getId(), c.getCategoryName()), Map::putAll);

        validateCategoryCounts(request.categories(), categoryMap);

        String categoryOptions = buildCategoryOptions(categoryMap);
        String categoryCounts = buildCategoryCounts(request.categories(), categoryMap);

        String systemText = aiProductGenerateTemplate.render(Map.of(
            "categoryOptions", categoryOptions,
            "categoryCounts", categoryCounts
        ));
        int expectedCount = request.categories().stream()
            .mapToInt(AiProductGenerateRequest.CategoryCount::count)
            .sum();
        AiProductSpecList specList = null;
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            specList = requestSpecList(systemText, expectedCount);
            if (specList == null || specList.items() == null || specList.items().isEmpty()) {
                continue;
            }
            boolean valid = true;
            for (AiProductSpec spec : specList.items()) {
                try {
                    validateSpec(spec, categoryMap);
                } catch (CustomException e) {
                    log.warn("AI 상품 스펙 검증 실패. attempt={}, reason={}", attempt, e.getMessage());
                    valid = false;
                    break;
                }
            }
            if (valid) {
                break;
            }
        }
        if (specList == null || specList.items() == null || specList.items().isEmpty()) {
            throw new CustomException("AI 상품 생성 결과가 비어 있습니다.");
        }

        List<ProductInfo> created = new ArrayList<>();
        for (AiProductSpec spec : specList.items()) {
            try {
                String description = buildAiDescription(spec);
                String fileGroupId = null;
                String fileUrl = null;
                try {
                    String imagePrompt = generateImagePrompt(spec.title(), description);
                    byte[] imageBytes = generateImageBytes(imagePrompt);
                    String fileName = "generated-" + UUID.randomUUID() + ".png";
                    MultipartFile file = new ByteArrayMultipartFile(
                        "files",
                        fileName,
                        "image/png",
                        imageBytes
                    );
                    var fileGroup = fileService.upload(List.of(file)).getData();
                    if (fileGroup != null) {
                        fileGroupId = fileGroup.fileGroupId();
                        fileUrl = (fileGroup.files() == null || fileGroup.files().isEmpty())
                            ? null
                            : fileGroup.files().get(0).filePath();
                    }
                } catch (Exception e) {
                    log.warn("AI 이미지 생성/업로드 실패. title={}, reason={}", spec.title(), e.getMessage());
                }

                ProductCommand command = new ProductCommand(
                    spec.title(),
                    description,
                    List.of(spec.category().code()),
                    fileGroupId,
                    fileUrl
                );
                created.add(productService.createProduct(command));
            } catch (Exception e) {
                log.warn("AI 상품 생성 실패. title={}, reason={}", spec.title(), e.getMessage());
            }
        }

        return created;
    }

    private static void validateSpec(AiProductSpec spec, Map<String, String> categoryMap) {
        if (spec == null || spec.category() == null) {
            throw new CustomException("AI 상품 생성 결과가 유효하지 않습니다.");
        }
        String code = spec.category().code();
        if (code == null || !categoryMap.containsKey(code)) {
            throw new CustomException("존재하지 않는 카테고리 코드가 포함되어 있습니다.");
        }
        if (spec.title() == null || spec.title().isBlank()) {
            throw new CustomException("AI 상품 제목이 비어 있습니다.");
        }
    }

    private String generateImagePrompt(String title, String description) {
        String systemText = aiProductImagePromptTemplate.render(Map.of(
            "title", title,
            "description", description
        ));
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
            .temperature(0.8)
            .toolChoice(buildToolChoice("product_image_prompt"))
            .build();
        ImagePromptResponse response = chatClient.prompt()
            .system(systemText)
            .user("이미지 프롬프트를 작성해줘.")
            .options(chatOptions)
            .tools(productAiTool)
            .call()
            .entity(ImagePromptResponse.class);
        if (response == null || response.prompt() == null || response.prompt().isBlank()) {
            throw new CustomException("이미지 프롬프트 생성에 실패했습니다.");
        }
        return response.prompt();
    }

    private byte[] generateImageBytes(String prompt) {
        String imagePrompt = buildImagePrompt(prompt);

        OpenAiImageOptions options = OpenAiImageOptions.builder()
            .width(1024)
            .height(1024)
            .quality("low")
            .build();

        ImageResponse response = imageModel.call(new ImagePrompt(imagePrompt, options));
        Image image = response.getResult().getOutput();
        if (image.getB64Json() != null && !image.getB64Json().isBlank()) {
            return Base64.getDecoder().decode(image.getB64Json());
        }
        if (image.getUrl() != null && !image.getUrl().isBlank()) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> entity = restTemplate.getForEntity(image.getUrl(), byte[].class);
            if (entity.getBody() != null) {
                return entity.getBody();
            }
        }
        throw new CustomException("이미지 생성 결과를 가져오지 못했습니다.");
    }

    private static String buildImagePrompt(String prompt) {
        return "Photorealistic product photo. "
            + prompt
            + " Studio lighting, neutral background, no text, no watermark, no people.";
    }

    private AiProductSpecList requestSpecList(String systemText, int expectedCount) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
            .temperature(0.8)
            .toolChoice(buildToolChoice("product_spec_list"))
            .responseFormat(buildSpecListResponseFormat(expectedCount))
            .build();
        String userText = "요청한 개수만큼 상품 정보를 생성해줘.";
        AiProductSpecList lastSpecList = null;
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String attemptUserText = attempt == 1
                ? userText
                : "이전 응답이 형식/개수 오류였습니다. 동일 조건으로 다시 생성해줘.";
            try {
                AiProductSpecList specList = chatClient.prompt()
                    .system(systemText)
                    .user(attemptUserText)
                    .options(chatOptions)
                    .tools(productAiTool)
                    .call()
                    .entity(AiProductSpecList.class);
                Integer actualCount = specList == null || specList.items() == null
                    ? null
                    : specList.items().size();
                if (specList != null && specList.items() != null && !specList.items().isEmpty()) {
                    lastSpecList = specList;
                }
                if (actualCount != null && actualCount == expectedCount) {
                    return specList;
                }
                log.warn(
                    "AI 상품 생성 결과 개수 불일치. expected={}, actual={}, attempt={}",
                    expectedCount,
                    actualCount,
                    attempt
                );
            } catch (Exception e) {
                log.warn("AI 상품 생성 결과 파싱 실패. attempt={}, reason={}", attempt, e.getMessage());
            }
        }
        if (lastSpecList != null) {
            return lastSpecList;
        }
        throw new CustomException("AI 상품 생성 결과가 유효하지 않습니다.");
    }

    private static ResponseFormat buildSpecListResponseFormat(int expectedCount) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("items"));

        Map<String, Object> itemSchema = new HashMap<>();
        itemSchema.put("type", "object");
        itemSchema.put("additionalProperties", false);
        itemSchema.put(
            "required",
            List.of(
                "category",
                "title",
                "summary",
                "condition",
                "features",
                "specs",
                "includedItems",
                "defects",
                "recommendedFor",
                "searchKeywords"
            )
        );

        Map<String, Object> categorySchema = Map.of(
            "type", "object",
            "additionalProperties", false,
            "required", List.of("code", "name"),
            "properties", Map.of(
                "code", Map.of("type", "string"),
                "name", Map.of("type", "string")
            )
        );
        Map<String, Object> conditionSchema = Map.of(
            "type", "object",
            "additionalProperties", false,
            "required", List.of("overall", "details"),
            "properties", Map.of(
                "overall", Map.of("type", "string"),
                "details", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string")
                )
            )
        );
        Map<String, Object> stringArraySchema = Map.of(
            "type", "array",
            "items", Map.of("type", "string")
        );

        Map<String, Object> itemProperties = new HashMap<>();
        itemProperties.put("category", categorySchema);
        itemProperties.put("title", Map.of("type", "string"));
        itemProperties.put("summary", Map.of("type", "string"));
        itemProperties.put("condition", conditionSchema);
        itemProperties.put("features", stringArraySchema);
        itemProperties.put("specs", stringArraySchema);
        itemProperties.put("includedItems", stringArraySchema);
        itemProperties.put("defects", stringArraySchema);
        itemProperties.put("recommendedFor", stringArraySchema);
        itemProperties.put("searchKeywords", stringArraySchema);
        itemSchema.put("properties", itemProperties);

        Map<String, Object> itemsSchema = new HashMap<>();
        itemsSchema.put("type", "array");
        itemsSchema.put("minItems", expectedCount);
        itemsSchema.put("maxItems", expectedCount);
        itemsSchema.put("items", itemSchema);

        schema.put("properties", Map.of("items", itemsSchema));

        ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
            .name("AiProductSpecList")
            .schema(schema)
            .strict(true)
            .build();
        return ResponseFormat.builder()
            .type(ResponseFormat.Type.JSON_SCHEMA)
            .jsonSchema(jsonSchema)
            .build();
    }

    private static Map<String, Object> buildToolChoice(String toolName) {
        return Map.of(
            "type", "function",
            "function", Map.of("name", toolName)
        );
    }

    private static String buildAiDescription(AiProductSpec draft) {
        List<String> blocks = new ArrayList<>();
        blocks.add("[상품 요약]");
        blocks.add(blankToEmpty(draft.summary()));
        blocks.add("");
        blocks.add("[상태]");
        blocks.add("- 종합: " + (draft.condition() == null ? "" : blankToEmpty(draft.condition().overall())));
        if (draft.condition() != null && draft.condition().details() != null) {
            for (String detail : draft.condition().details()) {
                blocks.add("- " + detail);
            }
        }

        pushList(blocks, "특징", safeList(draft.features()));
        pushList(blocks, "스펙", safeList(draft.specs()));
        pushList(blocks, "구성품", safeList(draft.includedItems()));
        pushList(blocks, "하자/주의", safeList(draft.defects()));
        pushList(blocks, "추천 대상", safeList(draft.recommendedFor()));
        pushList(blocks, "검색 키워드", safeList(draft.searchKeywords()));

        return String.join("\n", blocks).trim();
    }

    private static void pushList(List<String> blocks, String title, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        blocks.add("");
        blocks.add("[" + title + "]");
        for (String item : items) {
            blocks.add("- " + item);
        }
    }

    private static List<String> safeList(List<String> items) {
        return items == null ? List.of() : items;
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String buildCategoryOptions(Map<String, String> categories) {
        return categories.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + " | " + entry.getValue())
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");
    }

    private static String buildCategoryCounts(
        List<AiProductGenerateRequest.CategoryCount> categories,
        Map<String, String> categoryMap
    ) {
        return categories.stream()
            .map(item -> {
                String name = categoryMap.getOrDefault(item.categoryId(), "UNKNOWN");
                return "- " + item.categoryId() + " | " + name + " | count " + item.count();
            })
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");
    }

    private static void validateCategoryCounts(
        List<AiProductGenerateRequest.CategoryCount> categories,
        Map<String, String> categoryMap
    ) {
        for (AiProductGenerateRequest.CategoryCount item : categories) {
            if (item == null || item.categoryId() == null || item.categoryId().isBlank()) {
                throw new CustomException("카테고리 ID가 필요합니다.");
            }
            if (item.count() <= 0) {
                throw new CustomException("생성 개수는 1 이상이어야 합니다.");
            }
            if (!categoryMap.containsKey(item.categoryId())) {
                throw new CustomException("존재하지 않는 카테고리 ID가 포함되어 있습니다.");
            }
        }
    }
}
