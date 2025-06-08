package com.team901.CapstoneDesign.GPT.service;



import com.team901.CapstoneDesign.GPT.dto.Content;
import com.team901.CapstoneDesign.GPT.dto.YoutubeContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class IngredientExtractionService{
    private final GPTService gptService;
    private final NaverBlogCrawler naverBlogCrawler;
    private final BlogCrawler blogCrawler;



    /**
     * 진짜 주소 넣어서 식재료 반환
     * @param url
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Content getIngredient(String url) throws IOException, InterruptedException {
        String ingredient= gptService.getChatResponse(
                naverBlogCrawler.getNaverBlogContent(url)+"\n -----------\n 필요한 음식 재료만 콤마로 구분해서 알려줘. 특수기호를 쓰지말고 한글 텍스트로만 알려줘");
        return new Content(ingredient);
    }


    /**
     * 원시 주소를 입력하면 식재료 반환
     * @param url
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public List<String> getIngredient2(String url) throws IOException, InterruptedException {
//        String realurl=naverBlogCrawler.getMeta(url);
//        System.out.println("getIngredient2 주소 추출 곃과 : "+realurl);
//        String blogContent =naverBlogCrawler.getNaverBlogContent(realurl);
//        System.out.println("getIngredient2 블로그 글 추출 결과 : "+blogContent);
        String ingredient =gptService.getChatResponse(naverBlogCrawler.getContentBymetaurl(url)

                +"\n -----------\n 필요한 음식 재료만 콤마로 구분해서 알려줘. 특수기호를 쓰지말고 숫자와 한글 텍스트로만 알려줘");
        return extractIngredients(ingredient);
    }


    //설명란 내용과 gpt 결합
    public YoutubeContent youtube(String url) throws InterruptedException {
        String result=gptService.getChatResponse(String.valueOf(blogCrawler.getBlogData(url))+
                "\n -----------\n  재료만 ,로 구분해서 알려줘" );

        String title = blogCrawler.getYoutubeName(url);
        List<String> ingredients=extractIngredients(result);

        return new YoutubeContent(title,ingredients);
    }



    //gpt결과를 배열로 만드는 메소드
    public List<String> extractIngredients(String response) {
        return Arrays.stream(response.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

}
