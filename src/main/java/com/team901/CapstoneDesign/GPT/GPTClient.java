package com.team901.CapstoneDesign.GPT;

import com.team901.CapstoneDesign.GPT.GPTRequest;
import com.team901.CapstoneDesign.GPT.GPTResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "gptClient", url = "${gpt.api.url}",
        configuration = com.team901.CapstoneDesign.config.GPTFeignConfig.class)
public interface GPTClient {
    @PostMapping
    GPTResponse getGptResponse(GPTRequest request);
}