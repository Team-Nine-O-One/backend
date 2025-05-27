package com.team901.CapstoneDesign;

import com.team901.CapstoneDesign.GPT.dto.GPTRequest;
import com.team901.CapstoneDesign.GPT.dto.GPTResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "gptClient", url = "${gpt.api.url}",
        configuration = GPTFeignConfig.class)
public interface GPTClient {
    @PostMapping
    GPTResponse getGptResponse(GPTRequest request);
}