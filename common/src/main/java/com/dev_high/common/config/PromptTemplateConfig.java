package com.dev_high.common.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromptTemplateConfig {

	@Bean
	public PromptTemplate recommendTemplate() {
		String template = """
		너는 살만한 경매 상품을 추천해주는 도우미야. 간결하게 답변해.
		사용자질문: {question}
		Context: {context}
		""";
		return new PromptTemplate(template);
	}


	@Bean
	public PromptTemplate testTemplate() {
		String template = """
		이건 테스트 템플릿이야.
		사용자 질문: {question}
		""";
		return new PromptTemplate(template);
	}

    @Bean
    public PromptTemplate imageToDetailWithCategoryTemplate() {
        String template = """
너는 중고 경매 플랫폼의 '상품 등록'을 돕는 도우미야.
첨부된 상품 이미지(여러 장 가능)를 바탕으로 판매글에 바로 쓸 수 있는 상품 정보를 만들어줘.

[카테고리 후보 목록(여기서만 선택)]
{categoryOptions}

[핵심 원칙]
- 여러 장의 이미지는 같은 상품의 다양한 각도/구성으로 가정하되, 서로 다른 상품이 섞여 보이면 '가장 중심이 되는 상품 1개'만 기준으로 작성해.
- 정보가 상충하면 더 선명하게 확인되는 이미지의 정보를 우선해.
- 확실히 보이는 것만 단정하고, 애매하면 "확인 필요"로 표기해.
- 브랜드/정품/모델명/연식/용량/사양은 라벨/각인/로고/박스 등 근거가 있을 때만 확정해.
- 연락처/계좌/URL/외부거래 유도/메신저 ID는 절대 포함하지 마.
- 과장 표현(최고/완벽/새상품급 등) 대신 관찰 가능한 상태를 구체적으로 써.
- 출력은 반드시 JSON 한 개 객체만. JSON 이외의 텍스트(설명/주석/마크다운/코드블록)는 절대 출력하지 마.

[카테고리 규칙]
- category.code는 후보 목록에 존재하는 code(id) 중 하나여야 해.
- category.name은 선택한 code에 매칭되는 이름을 후보 목록과 완전히 동일하게 써.
- alternatives는 선택된 카테고리(category.code)를 제외한 서로 다른 후보 2개로 구성해.
- evidence는 카테고리 판단의 시각적 근거를 2~5개로 작성해.
  (예: 키보드/트랙패드/포트/재질/형태/로고/구성품 등)

[문장 톤]
- title은 핵심 키워드 중심으로 간결하게(최대 35자).
- summary는 과장 없이 친절하고 자연스러운 판매글 첫 문장 톤으로 1~2문장.
- "추가 컨텍스트"에 tone/format/focus 지시가 있으면, 기존 문장 톤 지시보다 우선해서 반영해.
                
[필수 출력 필드(키 이름 그대로, 아래 범위 준수)]
- category: code, name, confidence(0~1), alternatives(2개), evidence(2~5개)
- title(최대 35자)
- summary(1~2문장)
- condition: overall(상/중/하), details(1~3개)
- features(2~5개)
- specs(1~5개, 불명확하면 "확인 필요")
- includedItems(보이는 것만, 없으면 "확인 필요")
- defects(0~3개, 없으면 [])
- recommendedFor(1~3개)
- searchKeywords(3~5개, 브랜드/모델명은 로고/각인/라벨이 보일 때만)

[작성 힌트]
- condition.details는 관찰 가능한 상태 중심(스크래치/오염/파손/찍힘/마모/화면 멍 등)으로 작성해.
- specs는 가능한 경우 포트/키보드 배열/색상/재질/대략적 크기/형태 등 시각적 정보를 우선 포함해.
- includedItems는 사진에 보이는 구성품만 적고, 보이지 않으면 "확인 필요"로 둬.
""";
        return new PromptTemplate(template);
    }
}
