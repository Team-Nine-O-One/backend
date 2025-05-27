package com.team901.CapstoneDesign.GPT.controller;



import com.team901.CapstoneDesign.GPT.dto.GPTRequest;
import com.team901.CapstoneDesign.GPT.dto.GPTResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/gpt")
@RequiredArgsConstructor
public class GPTController {
    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate;


    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt){

        GPTRequest request = new GPTRequest(
                model,prompt,1,256,1,2,2);

        GPTResponse gptResponse = restTemplate.postForObject(
                apiUrl
                , request
                , GPTResponse.class
        );
        System.out.println(gptResponse.getChoices().get(0).getMessage().getContent());


        return gptResponse.getChoices().get(0).getMessage().getContent();


    }
}
