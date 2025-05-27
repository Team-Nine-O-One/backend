package com.team901.CapstoneDesign.GPT.service;


import com.team901.CapstoneDesign.GPTClient;
import com.team901.CapstoneDesign.GPT.dto.GPTRequest;
import com.team901.CapstoneDesign.GPT.dto.GPTResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GPTService {
    private final GPTClient gptClient;

    @Value("${gpt.model}")
    private String model;

    public String getChatResponse(String prompt) {
        GPTRequest request = new GPTRequest(model, prompt, 1, 256, 1, 2, 2);
        GPTResponse response = gptClient.getGptResponse(request);

        return response.getChoices().get(0).getMessage().getContent();
    }
}
