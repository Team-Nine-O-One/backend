package com.team901.CapstoneDesign.GPT.controller;



import com.team901.CapstoneDesign.GPT.dto.Content;
import com.team901.CapstoneDesign.GPT.dto.Product;
import com.team901.CapstoneDesign.GPT.service.BlogCrawler;
import com.team901.CapstoneDesign.GPT.service.IngredientExtractionService;
import com.team901.CapstoneDesign.GPT.service.NaverBlogCrawler;
import com.team901.CapstoneDesign.GPT.service.SeleniumNaverShoppingCrawler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
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

    //유튜브쇼츠 설명란 크롤링
    @GetMapping("/crawl1")
    public String crawl1(
            @RequestParam String url
    ) {
        try {
            return blogCrawler.getBlogData(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //유튜브 쇼츠 주소 넣고 재료 리스트 반환
    @GetMapping("/youtube")
    public List<String> youtube(@RequestParam String url){
        try {
            return ingredientExtractionService.youtube(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/crawl3")
    public Content crawl3(
            @RequestParam String url
    ) {
        try {
            return NaverBlogCrawler.getReal(url);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }


    //블로그 원 주소 인코딩해서 넣으면 재료 리스트 반환
    @GetMapping("/naverToProduct")
    public List<String> naverToProduct(
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
