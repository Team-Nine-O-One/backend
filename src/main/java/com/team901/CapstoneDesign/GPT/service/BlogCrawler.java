package com.team901.CapstoneDesign.GPT.service;




import com.team901.CapstoneDesign.GPT.dto.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

@Service
public class BlogCrawler {

    //유튜브 쇼츠 설명란 내용 크롤링
    public String getBlogData(String url) throws InterruptedException {
        // 크롤링 결과를 담을 리스트 초기화
        String description = "";

        // Selenium WebDriver 설정
        ChromeOptions options = new ChromeOptions(); // 크롬 옵션 객체 생성
        //options.addArguments("--headless=new");

        WebDriver driver = new ChromeDriver(options); // WebDriver 객체 생성

        try {
            // 네이버 쇼핑 검색 URL 생성
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div#snippet span") ));

            // 설명 텍스트 추출
            description = element.getAttribute("textContent");
            System.out.println("✅ 유튜브 설명 크롤링 성공:\n" + description);

            //System.out.println("✅ element.getAttribute('textContent'): " + element.getAttribute("textContent"));
            //System.out.println("✅ element.getAttribute('innerHTML'): " + element.getAttribute("innerHTML"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // ✅ 브라우저는 항상 종료
        }

        return description; // 크롤링 결과 반환
    }


    private Content parseContents(String html) {
        // Jsoup을 사용해 HTML 파싱
        Document doc = Jsoup.parse(html);

        String contents =extractContents(doc);

        // 모든 정보가 유효한 경우에만 Product 객체 생성
        if (contents== null ) return null;
        System.out.println("contents= " + contents);
        return new Content(contents);
    }


    private String extractContents(Document doc){
        Elements contentsElements = doc.select(
                "#plain-snippet-text"
        );
        return !contentsElements.isEmpty() ? contentsElements.text() : null;
    }


}

