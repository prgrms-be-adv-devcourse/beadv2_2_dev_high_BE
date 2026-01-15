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
- alternatives는 CategoryDto.AltDto 형태( code, name, confidence )로만 채워.
- alternatives.code는 후보 목록의 code(id), alternatives.name은 해당 code의 이름과 동일해야 해.
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

[필수 출력 JSON 스키마 예시]
{{
  "category": {{
    "code": "",
    "name": "",
    "confidence": 0.0,
    "alternatives": [
      {{"code": "", "name": "", "confidence": 0.0}},
      {{"code": "", "name": "", "confidence": 0.0}}
    ],
    "evidence": ["", ""]
  }},
  "title": "",
  "summary": "",
  "condition": {{
    "overall": "",
    "details": ["", ""]
  }},
  "features": ["", ""],
  "specs": ["", ""],
  "includedItems": ["", ""],
  "defects": [],
  "recommendedFor": ["", ""],
  "searchKeywords": ["", ""]
}}

[작성 힌트]
- condition.details는 관찰 가능한 상태 중심(스크래치/오염/파손/찍힘/마모/화면 멍 등)으로 작성해.
- specs는 가능한 경우 포트/키보드 배열/색상/재질/대략적 크기/형태 등 시각적 정보를 우선 포함해.
- includedItems는 사진에 보이는 구성품만 적고, 보이지 않으면 "확인 필요"로 둬.
""";
        return new PromptTemplate(template);
    }

    @Bean
    public PromptTemplate auctionRecommendationTemplate() {
        String template = """
너는 경매 시작가 추천을 돕는 도우미야. 아래 값을 참고해서 tool `auction_recommendation`을 반드시 호출해.
tool 호출 시 인자는 {{"price": number, "reason": "string"}} 형태여야 한다.
reason은 한국어로 간결하게 2~3문장으로 작성해.
dataNotes가 비어있지 않으면 반드시 이용해서 reason을 작성해.
referencePrice와 priceRange, 낙찰/경매 시작가 요약값을 기반으로 price를 판단해.
데이터가 부족하면 product 정보와 category 정보를 바탕으로 price를 추정해.
price는 정수로 출력하고, priceRange가 있다면 범위 안으로 맞춰.
price는 100원 단위로 반올림해 출력해.

- productId: {productId}
- productName: {productName}
- productDescription: {productDescription}
- categoryNames: {categoryNames}
- referencePrice: {referencePrice}
- priceRange: {priceRangeMin} ~ {priceRangeMax}
- winningPriceMin: {winningPriceMin}
- winningPriceMax: {winningPriceMax}
- winningPriceAvg: {winningPriceAvg}
- winningPriceMedian: {winningPriceMedian}
- auctionStartBidMin: {auctionStartBidMin}
- auctionStartBidMax: {auctionStartBidMax}
- auctionStartBidAvg: {auctionStartBidAvg}
- auctionStartBidMedian: {auctionStartBidMedian}
- similarCount: {similarCount}
- winningCount: {winningCount}
- auctionCount: {auctionCount}
- dataNotes: {dataNotes}
""";
        return new PromptTemplate(template);
    }

	@Bean
	public PromptTemplate recommendationTextTemplate() {
		String template = """
            너는 중고 경매 서비스의 추천 결과를 "한 줄"로 요약하는 역할이다.
            아래 추천 목록은 이미 유사도 기반으로 선택된 결과다.
            상품명, 카테고리, 설명(description), 유사도 점수를 근거로
            이 추천 결과 전체를 대표하는 요약 문장을 한국어로 1문장 작성하라.

            규칙:
            - 반드시 한 문장 (줄바꿈 금지)
            - 30~60자 내외
            - 과장 금지(확정 표현 금지). 예: "비슷해요", "유사해요" 정도만
            - 'AI'라는 단어 사용 금지
            - 설명(description)에 나타난 공통 특징이 있으면 반영
            - 추천 결과를 다시 판단하지 말고 "이유"만 설명
            - 출력은 요약 문장만. 따옴표/코드블록/머리말/접두어 금지

            추천 목록(JSON):
            {itemsJson}
            """;
		return new PromptTemplate(template);
	}
}
