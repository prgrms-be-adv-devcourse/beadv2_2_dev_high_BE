package com.dev_high.common.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromptTemplateConfig {


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
    public PromptTemplate aiProductGenerateTemplate() {
        String template = """
            너는 중고 경매 플랫폼의 상품 등록을 돕는 생성형 도우미야.
            입력된 카테고리별 생성 개수에 맞춰, 실제로 팔릴 법한 상품 정보를 만들어줘.
            tool `product_spec_list`를 반드시 호출해.

            [카테고리 후보 목록(여기서만 선택)]
            {categoryOptions}

            [카테고리별 생성 개수]
            {categoryCounts}

            [핵심 원칙]
            - 출력은 반드시 JSON 한 개 객체만. JSON 이외의 텍스트는 절대 출력하지 마.
            - items 배열 길이는 카테고리별 생성 개수 합계와 정확히 일치해야 해.
            - category.code는 후보 목록의 code(id)만 사용해.
            - category.name은 선택한 code에 해당하는 이름과 완전히 동일해야 해.
            - 상품은 서로 중복되지 않게 하고, 제목/스펙/키워드를 다양하게 구성해.
            - 너무 평범한 조합만 반복하지 말고, 실제로 팔릴 법한 선에서 적당히 창의적으로 만들어.
            - title은 35자 이내.
            - summary는 실제 중고 판매글 톤으로 1~2문장.
            - recommendedStartBid는 원화 정수, 0보다 커야 해.
            - auctionDurationHours는 1~72 사이의 정수(시간 단위).
            - recommendedStartBid는 경매 특성을 반영해, 추정 시세 대비 40~60%를 기본으로 잡아.
            - 인기/희소 상품은 60~80%, 상태가 나쁘면 10~30%로 조정해.
            - recommendedStartBid는 100원 단위로 반올림하고, 지나치게 낮은 값은 피한다.
            - condition.overall은 상/중/하 중 하나.
            - condition.details는 1~3개.
            - features/specs/includedItems/recommendedFor/searchKeywords는 1~5개.
            - defects는 0~3개, 없으면 [].

            [필수 출력 JSON 스키마]
            {{
              "items": [
                {{
                  "category": {{"code": "", "name": ""}},
                  "title": "",
                  "summary": "",
                  "recommendedStartBid": 0,
                  "auctionDurationHours": 0,
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
              ]
            }}
            """;
        return new PromptTemplate(template);
    }

    @Bean
    public PromptTemplate aiProductImagePromptTemplate() {
        String template = """
            너는 이미지 생성 프롬프트를 만드는 도우미야.
            아래 상품 제목과 설명을 기반으로 실사에 가까운 제품 단독 촬영용 프롬프트를 작성하고
            tool `product_image_prompt`를 반드시 호출해.

            [상품 제목]
            {title}

            [상품 설명]
            {description}

            [프롬프트 지침]
            - 실사 사진, 스튜디오 조명, 중립 배경
            - 텍스트/워터마크/로고/사람 없음
            - 제품이 한 개만 보이도록
            - 폭력/무기/선정/미성년/브랜드 노출이 연상되는 표현 금지
            - 1~2문장
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
			너는 중고 경매 서비스의 추천 결과를 "한 줄"로 요약한다.
			아래 추천 목록은 이미 유사도 기반으로 선택된 결과지만,
			추천 상품 간 공통점이 약하거나 명확하지 않을 수도 있다.
	
			상품명, 카테고리, 설명(description), 유사도 점수를 참고하되
			억지로 공통점을 만들어내지 말고,
			자연스럽게 설명 가능한 수준에서만 추천 이유를 작성하라.
	
			출력 규칙(매우 중요):
			- 출력은 오직 한 문장만 (줄바꿈 금지)
			- 30~60자
			- 따옴표/코드블록/머리말(예: "요약:") 금지
			- 과장 금지(확정 표현 금지): "딱 맞는", "완벽한", "확실한" 등 금지
			- 개인화가 불명확하면 보편적인 추천 표현 사용:
			  "함께 살펴볼 만한", "관심을 가질 수 있는" 등
			- 'AI' 단어 사용 금지
			- description에 공통 특징이 명확할 때만 반영
			- 추천 결과를 재평가/판단하지 말고, 왜 함께 추천되었는지 이유만 작성
	
			추천 목록(JSON):
			{itemsJson}
			
			이제 규칙을 만족하는 한 문장만 출력하라.
			""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate userIntentTemplate() {
		String template = """
			너는 상품 챗봇의 응답 생성기다.
			
			사용자의 입력을 분석하여:
			1. 의도(intent)를 하나 선택하고
			2. 그에 맞는 최종 답변(answer)을 생성하라.
			
			의도(intent)는 반드시 아래 중 하나여야 한다.
			- GREETING: 인사
			- PRODUCT: 상품 문의, 추천, 비교, 구매 관련 질문
			- GENERIC_RECOMMENDATION: 조건 없이 아무거나 추천 요청
			- SERVICE: 배송, 결제, 환불, 계정, 이용 방법 문의
			- NON_PRODUCT: 상품과 직접 관련 없는 일반 질문
			- OFF_TOPIC: 서비스 목적과 무관하거나 엉뚱한 질문
			- ABUSIVE: 욕설, 비방, 공격적인 표현
			
			규칙:
			- intent는 반드시 하나만 선택하라
			- 욕설이나 공격적인 표현이 있으면 다른 조건보다 ABUSIVE를 우선한다
			- 조건/필터 없이 "아무거나 추천", "랜덤 추천", "요즘 뭐 추천", "인기 상품 추천"처럼 구체 조건 없이 추천을 요청하면 GENERIC_RECOMMENDATION을 선택한다
			- answer는 한국어로 1~3문장으로 작성하라
			- 장황한 설명이나 불필요한 말은 하지 마라
			- 사과가 필요한 경우에만 간단히 사과하라
			
			사용자 입력:
			"{message}"
			
			""";

		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate greetingPromptTemplate() {
		String template= """
				너는 상품 서비스 챗봇이다.
				
					규칙:
					- 간단하고 친절하게 인사한다
					- 불필요한 설명은 하지 않는다
					- 한국어로 1문장으로 응답한다
				""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate productPromptTemplate() {
		String template = """
				너는 상품 추천 및 안내를 담당하는 챗봇이다.
				
				아래는 이미 확정된 판단 결과이다.
				- intent: PRODUCT
				- reasoning: {reasoning}
				
					규칙:
					- productIds에는 userText의 context에서 관련 있다고 판단되는 productId만 담는다.
					- answer에는 추천 이유 또는 안내를 담는다.
					- 만약 userText의 context 중에 사용자 질문을 충족시킬 상품이 없다면, 과감하게 추천상품이 없다고 하라
					- userText의 context로 전달된 상품 중 일부 상품이 관련없다고 판단되면, 해당 상품들은 productIds에서 제외한다
					- 상품 추천, 비교, 구매 관련 질문에만 답한다
					- "아무거나 추천해줘"처럼 막연한 요청이면 먼저 선호 조건(가격, 용도, 예산, 카테고리 등)을 질문해 구체화한다
					- 필요하면 추가 질문을 통해 요구사항을 구체화한다
					- 추측하지 말고, 모르면 질문하라
					- 상품 추천은 오직 전달된 context를 가지고만 한다
					- 존재하지않는 상품정보를 만들거나 추천하지 마라
					- 한국어로 1~3문장으로 응답한다
					- intent를 변경하거나 언급하지 마라
					- 응답은 JSON 객체 하나이며, 필드는 answer, productIds만 사용한다
					""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate servicePromptTemplate() {
		String template= """
				너는 고객 지원 챗봇이다.
				
				아래는 이미 확정된 판단 결과이다.
				- intent: SERVICE
				- reasoning: {reasoning}
				
					규칙:
					- 먼저 불편에 공감한다
					- 해결 절차를 명확히 안내한다
					- 책임을 회피하는 표현을 사용하지 않는다
					- 한국어로 1~3문장으로 응답한다
					""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate nonProductPromptTemplate() {
		String template= """
				너는 상품 서비스 챗봇이다.
				
				아래 질문은 상품과 직접 관련이 없다.
				
					규칙:
					- 상품 또는 서비스 관련 질문으로 유도한다
					- 한국어로 1~2문장으로 응답한다

					""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate offTopicPromptTemplate() {
		String template= """
			너는 상품 서비스 챗봇이다.
			
			규칙:

			- 해당 질문은 이 서비스의 범위를 벗어났음을 알린다
			- 추가 설명이나 대화 확장은 하지 않는다
			- 집에 가고 싶다는 식의 간단한 일상적 대화는 재치있게 대답하며, 서비스 및 상품에 대한 질문으로 유도한다.
			""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate abusivePromptTemplate() {
		String template = """
				너는 고객 응대 챗봇이다.
							
				규칙:
				- 공격적인 표현에는 감정적으로 반응하지 않는다
				- 짧고 중립적으로 응답한다
				- 한국어로 1문장으로 응답한다
				""";
		return new PromptTemplate(template);
	}

	@Bean
	public PromptTemplate extractKeywordsPromptTemplate() {
		String template = """
			너는 전자상거래 상품 설명에서 "검색/임베딩"에 사용할 핵심 키워드를 추출하는 엔진이다.
			
			규칙:
			- 출력은 반드시 JSON만 반환한다.
			- 문장 생성 금지. 조사/어미/형용사 문장(예: 추천, 최고, 가성비, 함께 즐길) 금지.
			- 키워드는 명사/상품속성/규격/재질/브랜드/모델/용도/대상/구성품 위주로 뽑는다.
			- 의미가 거의 같은 단어는 하나로 통합한다.
			- 일반 상투어(중고, 판매, 정리, 상태좋음, 추천, 인기, 가성비, 최상, 득템 등)는 제외한다.
			- 키워드는 최대 {limit}개

			상품 설명:
			{description}
		""";
		return new PromptTemplate(template);
	}
}
