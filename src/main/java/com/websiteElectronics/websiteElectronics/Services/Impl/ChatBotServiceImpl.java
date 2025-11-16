package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.ChatRequest;
import com.websiteElectronics.websiteElectronics.Dtos.ChatResponse;
import com.websiteElectronics.websiteElectronics.Services.ChatBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatBotServiceImpl implements ChatBotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatBotServiceImpl.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String apiUrl;

    private final WebClient webClient;

    public ChatBotServiceImpl(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    @Override
    public ChatResponse sendMessage(ChatRequest chatRequest) {
        try{
            String conversationId = chatRequest.getConversationId() != null
                    ? chatRequest.getConversationId()
                    : UUID.randomUUID().toString();

            logger.info("conversationId: {}", conversationId);

            Map<String, Object> requestBody = buildGemini(chatRequest.getMessage());

            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String aiMess = parseGeminiResponse(response);

            return ChatResponse.builder()
                    .message(aiMess)
                    .conversationId(conversationId)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }catch (Exception e){
            return ChatResponse.builder()
                    .error(e.getMessage())
                    .build();
        }
    }

    private Map<String, Object> buildGemini(String message) {
        Map<String, Object> request = new HashMap<>();

        Map<String, Object> content = new HashMap<>();

        Map<String, String> part = new HashMap<>();

        part.put("text", message);

        content.put("parts", List.of(part));

        request.put("contents", List.of(content));

        return request;
    }

    private String parseGeminiResponse(String response) {
        try{
            int candidatesStart = response.indexOf("\"candidates\"");
            if (candidatesStart == -1) {
                return "Error: No candidates in response";
            }

            int textStart = response.indexOf("\"text\": \"", candidatesStart) + 9;
            int textEnd = response.indexOf("\"", textStart);
            
            if (textStart > 8 && textEnd > textStart) {
                String text = response.substring(textStart, textEnd);
                text = text.replace("\\n", "\n")
                          .replace("\\r", "\r")
                          .replace("\\t", "\t")
                          .replace("\\\"", "\"");
                return text;
            }
            
            return "Error: Could not parse text from response";
        } catch (Exception e){
            logger.error("Error parsing Gemini response", e);
            return "Error: " + e.getMessage();
        }
    }
}
