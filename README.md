# Spring WebFlux + Spring AI ChatClient

Spring WebFlux와 Spring AI `ChatClient`를 사용해 LLM 응답을 **SSE(Server-Sent Events)로 스트리밍**하는 예제 프로젝트입니다.
기본 프로필에서는 OpenAI, `test` 프로필에서는 로컬 Ollama 모델을 사용합니다.

## 기술 스택

| 구분 | 버전 |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.1 |
| Spring AI | 2.0.1 (OpenAI, Ollama starter) |
| Web | Spring WebFlux (Reactor `Flux`) |
| Build | Gradle (Kotlin DSL) |

## 프로젝트 구조

```
src/main/java/com/back
├── BackApplication.java
├── ai
│   ├── controller/AiController.java   # GET /api/v1/ai/chat (SSE)
│   └── service/ChatService.java       # ChatClient로 스트리밍 응답 생성
└── global/config/AiConfig.java        # 프로필별 ChatClient 빈 등록

src/main/resources
├── application.yaml        # 기본 설정 (.env import, OpenAI API 키)
└── application-test.yaml   # test 프로필: OpenAI 자동 설정 제외, Ollama 모델 지정
```

## 프로필별 모델

| 프로필 | ChatClient | 설정 |
| --- | --- | --- |
| `default` | OpenAI | `OPENAI_API_KEY` 환경 변수 필요 |
| `test` | Ollama | `http://localhost:11434`, 모델 `qwen3.8:27b` |

`test` 프로필에서는 `application-test.yaml`이 OpenAI 관련 자동 설정을 모두 제외하므로, 컨텍스트에 Ollama `ChatModel`만 남습니다.

## 시작하기

### 1. OpenAI 사용 (default 프로필)

템플릿을 복사해 `.env` 파일을 만들고 API 키를 넣습니다. (`.env`는 `.gitignore`에 포함되어 있습니다.)

```bash
cp .env.default .env
```

```properties
OPENAI_API_KEY=sk-...
```

`application.yaml`이 `optional:file:.env[.properties]`로 이 파일을 읽어옵니다.

```bash
./gradlew bootRun
```

### 2. Ollama 사용 (test 프로필)

Docker Compose로 Ollama를 띄우고 모델을 받습니다.

```bash
docker compose up -d
docker compose exec ollama ollama pull qwen3.8:27b
```

다른 모델을 쓰려면 `application-test.yaml`의 `spring.ai.ollama.chat.model` 값을 바꿉니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

## API

### `GET /api/v1/ai/chat`

메시지를 보내면 모델 응답을 토큰 단위로 SSE 스트리밍합니다.

| 파라미터 | 위치 | 필수 | 설명 |
| --- | --- | --- | --- |
| `message` | query | O | 사용자 메시지 |

- Response `Content-Type`: `text/event-stream`

```bash
curl -N "http://localhost:8080/api/v1/ai/chat?message=안녕하세요"
```

```
data:안

data:녕하세요

data:!
...
```

## 테스트

`BackApplicationTests`에는 실제 모델을 호출하는 통합 테스트 두 개가 있습니다.

| 테스트 | 내용 |
| --- | --- |
| `t1` | `ChatService.streamChatResponse()` 결과를 모아 비어 있지 않은지 확인 |
| `t2` | `WebTestClient`로 `/api/v1/ai/chat` SSE 엔드포인트 호출 후 200 응답과 본문 확인 |

```bash
./gradlew test
```

테스트는 실제 LLM을 호출하므로 OpenAI API 키(default 프로필) 또는 실행 중인 Ollama(test 프로필)가 필요합니다.
Ollama로 테스트하려면 테스트 클래스의 `@ActiveProfiles("test")` 주석을 해제하세요.
