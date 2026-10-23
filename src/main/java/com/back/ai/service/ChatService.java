package com.back.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    @Autowired
    private ChatClient chatClient;

    public Flux<String> streamChatResponse(String userMessage){
        Message message = UserMessage.builder().text(userMessage).build();
        return chatClient.prompt().messages(message).stream().content();
    }
}
