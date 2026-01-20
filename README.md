# 🧩 Programmers Semi Project – Backend

Spring Boot 기반 **경매형 중고거래 플랫폼 백엔드**입니다.  
각 도메인은 독립 실행 가능한 멀티 모듈(MSA)로 구성되며,  
Kubernetes **2-Node 클러스터 환경**에서 운영됩니다.

---

## 📌 프로젝트 개요

### 서비스 목적
- 경매 기반 중고거래 플랫폼 구현  
  (상품 등록 → 경매 → 입찰 → 결제/정산 → 알림)

### 기술적 목표
- Spring Cloud 기반 MSA 설계 및 구현
- DDD(Bounded Context) 기준의 도메인 분리
- Kafka 기반 이벤트 중심 아키텍처
- WebSocket 기반 실시간 경매 처리

### 운영 / 인프라 목표
- Kubernetes 멀티노드 클러스터 구축
- 무중단 배포(Rolling Update)
- GitHub Actions 기반 CI/CD
- 중앙 설정 관리(Config Server)

---

## 🛠 주요 기술 스택
- Spring Boot / Spring Cloud (Gateway, Config, Eureka)
- Spring Security (JWT), WebFlux
- Kafka, WebSocket(STOMP)
- PostgreSQL, Redis, Elasticsearch
- Spring AI + pgvector
- Kubernetes, GitHub Actions

---

## 🏗 아키텍처 요약

- **API Gateway**
    - 단일 진입점
    - 인증 / 라우팅 / Swagger 집계

- **Config Server**
    - Git 기반 중앙 설정 관리

- **Discovery**
    - Eureka 기반 서비스 등록 / 발견

### 서비스 통신
- 동기: REST (Gateway 경유)
- 비동기: Kafka 이벤트

### 기타 구성
- 실시간 처리: WebSocket / STOMP
- 검색: Elasticsearch
- 데이터 저장소
    - PostgreSQL
    - Redis
    - Elasticsearch
    - AWS S3 (파일)

---

## ☸ Kubernetes 클러스터 구성

본 프로젝트는 **2-Node Kubernetes 클러스터**로 구성됩니다.  
노드별 역할을 분리하여 **인프라 Pod**와 **비즈니스 서비스 Pod**를 운영합니다.

###  노드 구성

| Node | 역할 |
|---|---|
| **Control Plane Node** | Control Plane + 인프라 Pod |
| **Worker Node** | 비즈니스 서비스 Pod |

---

###  Control Plane Node
- Kubernetes Control Plane
    - API Server
    - Scheduler
    - Controller Manager
- 인프라 관련 Pod
    - PostgreSQL
    - Redis
    - Kafka
    - Elasticsearch

> 학습 및 리소스 제약 환경을 고려하여  
> Control Plane Node에 인프라 Pod를 함께 운영

---

###  Worker Node
- 비즈니스 도메인 서비스 Pod 전용
    - apigateway
    - discovery
    - config
    - auction
    - product
    - user
    - deposit
    - settlement
    - search

---

###  배포 방식
- Deployment 기반 Rolling Update
- 서비스별 Replica 조정 가능
- Config 변경 시 Pod 재기동 방식으로 반영

---

## 📦 모듈 구성

```text
backend
|-- apigateway
|-- auction
|-- common
|-- config
|-- discovery
|-- deposit
|-- product
|-- search
|-- settlement
`-- user
```

- 각 서비스는 **독립 실행 및 배포 가능**
- 공통 코드(DTO, Exception, Kafka Util 등)는 `common` 모듈로 분리

---

## 🧠 주요 서비스 책임

### apigateway
- 모든 요청의 단일 진입점
- JWT 인증 / 인가
- Swagger 그룹 집계

### auction
- 경매 / 입찰 / 참여 관리
- WebSocket 기반 실시간 경매
- Kafka 이벤트 발행 / 수신

### product
- 상품 / 카테고리 관리
- 최신 경매 ID 갱신 및 내부 조회 API 제공

### search
- Elasticsearch 기반 경매 검색
- Kafka 이벤트 기반 인덱스 동기화

### deposit
- 예치금 계좌 / 결제 / 환불
- Toss 결제 연동

### settlement
- 정산 이력 / 요약 조회
- 정산 등록 / 집계 배치

### user
- 인증 / 토큰 발급
- 사용자 / 판매자 관리
- 위시리스트 기능

---

## 🔐 보안
- 외부 요청은 API Gateway만 허용
- JWT 기반 인증/인가를 Gateway에서 검증
- Access/Refresh 토큰 분리 운용

### 인증 / 인가

- **JWT 기반 인증**
    - 사용자 로그인 시 Access Token / Refresh Token 발급
    - Gateway에서 JWT 검증 후 서비스로 요청 전달
- **Gateway 단 인증 처리**
  - 단일 진입점(Single Entry Point)
  - JWT 검증 및 사용자 식별
  - 인증 필요/불필요 API 분리
  - Swagger 접근 제어 (환경별 설정)
- **Gateway 단 인가 처리**
  - DB의 `endpoint`, `endpoint_role`, `user.role` 정보를 기반으로 엔드포인트별 인가 정책을 로드
  - 메서드/경로 패턴 매칭으로 대상 엔드포인트를 결정하고 `auth_required` 여부 확인
  - JWT 역할(roles)과 엔드포인트 허용 역할을 비교하여 인가 결정

###  서비스 간 통신 보안

- 외부 접근은 **Gateway만 허용**
- 내부 서비스 간 통신은:
    - REST (Gateway 경유)
    - Kafka 이벤트 기반 비동기 처리

---

## 📡 Kafka 비동기 통신
- 서비스 간 상태 동기화와 후처리를 이벤트로 연결
- 주요 흐름: 경매/입찰/주문/정산/알림 상태를 Kafka로 전달
- 이벤트 소비 서비스가 필요한 도메인 데이터만 갱신

### 설계 목적
- 서비스 간 직접 호출 최소화
- 트랜잭션 이후 후처리 로직 분리
- 이벤트 소비 주체를 유연하게 확장 가능하도록 설계

### 활용 방식
- 경매, 입찰, 주문, 정산, 알림 등 **상태 변화가 발생하는 시점에 이벤트 발행**
- 이벤트를 필요로 하는 서비스만 구독하여 처리
- 이벤트 소비 서비스는 **자신의 도메인 데이터만 갱신**

### 주요 이벤트 흐름 예시
- 입찰 생성 / 수정 / 취소
- 주문 생성 / 상태 변경
- 정산 완료
- 알림 생성 요청

###  이벤트 기반 처리 예시

- 상품 생성 및 수정

Auction Service→ Kafka Event 발행→ Search Service (인덱스 갱신)→ Notification Service (알림 생성)

---

## 🔍 Elasticsearch 활용
- 경매 검색 전용 인덱스를 운영
- 키워드/카테고리/상태/가격/기간 조건으로 검색
- Kafka 이벤트로 인덱스 생성/수정/삭제 동기화

### 설계 특징
- 경매 데이터 검색 전용 인덱스 운영
- 복합 조건 검색 지원
    - 키워드
    - 카테고리
    - 경매 상태
    - 가격 범위
    - 기간 조건
- 정렬 및 페이징 최적화

### 데이터 동기화 전략
- 경매/상품/입찰 상태 변경 시 Kafka 이벤트 발행
- Search Service에서 이벤트를 소비하여
    - 인덱스 생성
    - 인덱스 수정
    - 인덱스 삭제
- **DB ↔ Elasticsearch 직접 동기화 로직 제거**
---

## 🤖 AI 활용

### 1. 상품 (product)
- Spring AI 기반 ChatClient/VectorStore(pgvector) 사용
- 상품 임베딩/벡터 검색으로 유사도 기반 추천 제공
- RAG 추천: 유사 검색 결과를 컨텍스트로 LLM 답변 및 추천 상품 생성
- 사용자 의도 분류로 추천/안내/응답 템플릿 분기
- 이미지 기반 상품 상세 초안 생성

### 2. 검색 (search)
- Spring AI EmbeddingModel로 상품 텍스트 임베딩 생성/갱신
- Elasticsearch `dense_vector`에 임베딩 저장 후 kNN 유사 검색
- 위시리스트 임베딩 평균 벡터로 추천 후보 검색
- 추천 결과 요약 문장을 Spring AI ChatClient로 생성

### 3. 경매 (auction)
- Spring AI ChatClient + Tool 기반 경매 시작가 추천
- 유사 상품/낙찰 데이터/가격 범위를 프롬프트로 전달해 추천값/사유 생성
- `auction.recommendation.ai-enabled` 설정으로 AI 추천 사용 여부 제어

## ▶ 실행 방법

```bash
# 전체 빌드
./gradlew build

# 서비스 개별 실행
./gradlew :product:bootRun

# 로컬 구동 시 discovery->config 순으로 구동.
#.env 필요
```

---
## 👥 팀 구성 및 역할
| 이름 | 역할                | 주요 담당                          |
|---|-------------------|--------------------------------|
| **이종탄** | 팀장, Backend       | PG 결제 연동, 예치금 기능 구현            |
| **전다윤** | 서기, Backend       | Spring Security, Elasticsearch 기능 구현 |
| **김근환** | Frontend, Backend | 인프라 구성, 경매 및 주문 기능 구현          |
| **박다빈** | Backend           | 인프라 구성, 상품 기능 구현               |
