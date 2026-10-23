package com.back;

import com.back.ai.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 스프링 부트 통합 테스트 - 전체 애플리케이션 컨텍스트를 로드
@ActiveProfiles("test") // application-test.yml 설정 파일 사용
@AutoConfigureWebTestClient
class BackApplicationTests {

    @Autowired
    private ChatService chatService; // 테스트할 ChatService 빈 주입

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Ollama Chat 클라이언트 테스트") // 테스트 이름 지정
    void t1(){
        assertDoesNotThrow(()->{ // 예외가 발생하지 않아야 테스트 통과

            String response = chatService.streamChatResponse("Hello, Ollama!")
                    // Ollama에 메시지 전송, Flux<String> 반환 (스트리밍 응답)

                    .collectList()
                    // Flux의 모든 요소를 List로 수집 → Mono<List<String>>

                    .map(list -> String.join("", list))
                    // 리스트의 문자열들을 하나로 합침 → Mono<String>

                    .block();
            // 비동기 → 동기 변환, 결과가 올 때까지 대기

            assertNotNull(response); // 응답이 null이 아닌지 확인
            assertFalse(response.isEmpty()); // 응답이 비어있지 않은지 확인
            System.out.println("Ollama Response: " + response); // 결과 출력
        });
    }

    @Test
    @DisplayName("SSE 스트리밍 테스트")
    void t2() {
        assertDoesNotThrow(() -> {
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/ai/chat")
                            .queryParam("message", "Hello, SSE!")
                            .build())
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(String.class)
                    .getResponseBody()
                    .collectList()
                    .map(list -> String.join("", list))
                    .doOnNext(response -> {
                        assertNotNull(response);
                        assertFalse(response.isEmpty());
                        System.out.println("SSE Response: " + response);
                    })
                    .block();
        });
    }
}
