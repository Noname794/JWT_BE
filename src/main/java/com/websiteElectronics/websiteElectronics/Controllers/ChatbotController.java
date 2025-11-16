package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.ChatRequest;
import com.websiteElectronics.websiteElectronics.Dtos.ChatResponse;
import com.websiteElectronics.websiteElectronics.Services.ChatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatBot")
public class ChatbotController {
    @Autowired
    private ChatBotService chatBotService;

    @PostMapping("/sendMessage")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
        ChatResponse response = chatBotService.sendMessage(chatRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<String> check(){
        return ResponseEntity.ok("Ok");
    }
}
