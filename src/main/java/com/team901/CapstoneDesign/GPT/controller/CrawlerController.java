package com.team901.CapstoneDesign.GPT.controller;



import com.team901.CapstoneDesign.GPT.dto.Content;
import com.team901.CapstoneDesign.GPT.dto.Product;
import com.team901.CapstoneDesign.GPT.dto.YoutubeContent;
import com.team901.CapstoneDesign.GPT.service.BlogCrawler;
import com.team901.CapstoneDesign.GPT.service.IngredientExtractionService;
import com.team901.CapstoneDesign.GPT.service.NaverBlogCrawler;
import com.team901.CapstoneDesign.GPT.service.SeleniumNaverShoppingCrawler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/crawler")
@Tag(name = "Ingredient Extraction", description = "레시피 url로 재료 추출 API")
public class CrawlerController {

    private final SeleniumNaverShoppingCrawler seleniumCrawler;

    private final BlogCrawler blogCrawler;
    private final IngredientExtractionService ingredientExtractionService;

    public CrawlerController(SeleniumNaverShoppingCrawler seleniumCrawler, BlogCrawler blogCrawler, IngredientExtractionService ingredientExtractionService) {
        this.seleniumCrawler = seleniumCrawler;
        this.blogCrawler = blogCrawler;
        this.ingredientExtractionService = ingredientExtractionService;
    }


    //네이버 쇼핑 크롤링
    @GetMapping("/crawl2")
    public List<Product> crawl2(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page) {
        try {
            return seleniumCrawler.getShoppingData(keyword, page);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    //유튜브쇼츠 설명란 크롤링
//    @GetMapping("/crawl1")
//    public String crawl1(
//            @RequestParam String url
//    ) {
//        try {
//            return blogCrawler.getBlogData(url);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


//    @GetMapping("/youtubeName")
//    public String youtubeName(
//            @RequestParam String url
//    ) {
//        try {
//            return blogCrawler.getYoutubeName(url);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }



    //유튜브 쇼츠 주소 넣고 재료 리스트 반환
    @GetMapping("/youtube")
    @Operation(
            summary = "유튜브 쇼츠에서 재료 추출",
            description = "유튜브 쇼츠 URL을 입력하면 영상 설명을 분석하여 요리 재료 목록을 추출하고 영상 제목과 함께 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 재료를 추출하였습니다."),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
                    @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
            }
    )
    public YoutubeContent youtube(@Parameter(description = "유튜브 쇼츠 영상 URL", example = "https://www.youtube.com/shorts/5CuOskioLNE")
            @RequestParam String url){
        try {
            return ingredientExtractionService.youtube(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    @GetMapping("/crawl3")
//    public Content crawl3(
//            @RequestParam String url
//    ) {
//        try {
//            return NaverBlogCrawler.getReal(url);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//
//    }


    //블로그 원 주소 인코딩해서 넣으면 재료 리스트 반환
    @GetMapping("/naverToProduct")
    @Operation(
            summary = "네이버 레시피 블로그에서 재료 추출",
            description = "네이버 블로그 URL을 입력하면 요리 재료 목록을 반환합니다."
    )
    public List<String> naverToProduct(@Parameter(description = "네이버 블로그 URL", example = "https://www.youtube.com/shorts/5CuOskioLNE")
            @RequestParam String url
    ){
        try {
            return ingredientExtractionService.getIngredient2(url);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
