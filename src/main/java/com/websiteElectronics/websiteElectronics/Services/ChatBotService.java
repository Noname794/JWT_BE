package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.ChatRequest;
import com.websiteElectronics.websiteElectronics.Dtos.ChatResponse;

public interface ChatBotService {
    public ChatResponse sendMessage(ChatRequest chatRequest);
}
