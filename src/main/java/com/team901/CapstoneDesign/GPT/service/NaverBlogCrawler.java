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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class NaverBlogCrawler {
    public  String getMeta(String url) throws IOException {
        // 네이버 블로그 url로 document가져오기
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/90.0.4430.212 Safari/537.36")
                .referrer("https://www.naver.com")
                .get();
        System.out.println("✅ 첫 번째 Document 로딩 완료");

        // iframe 태그에 있는 진짜 블로그 주소 가져오기
        Elements iframes = doc.select("iframe#mainFrame");
        System.out.println("✅ iframe 추출 결과: " + iframes);
        String src = iframes.attr("src");
        //진짜 블로그 주소 document 가져오기
        String url2 = "http://blog.naver.com" + src;
        System.out.println("✅ iframe 내부 src 추출: " + src);
        System.out.println("✅ 실제 블로그 주소: " + url2);

        return url2;
        //Document doc2 = Jsoup.connect(url2).get();

        // System.out.println("주소 확인용 : " + url2);
        //System.out.println("doc2 : "+doc2);
        // 블로그에서 원하는 블로그 페이지 가져오기
        // String[] blog_logNo = src.split("&");
        // String[] logNo_split = blog_logNo[1].split("=");
        // String logNo = logNo_split[1];

        // 찾고자 하는 블로그 본문 가져오기
        //  String real_blog_addr = "div#post-view" + logNo;

        //   Elements blog_element = doc2.select(real_blog_addr);

        //System.out.println("내용 : " + blog_element);
        // 블로그 썸네일 가져오기
        // String og_image = doc2.select("meta[property=og:image]").get(0).attr("content");
        //System.out.println("og_image : " + og_image);
    }

    public static Content getReal(String url) throws InterruptedException {
        StringBuilder result = new StringBuilder();
        // Selenium WebDriver 설정
        ChromeOptions options = new ChromeOptions(); // 크롬 옵션 객체 생성
        // 사용자 에이전트 설정
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Safari/537.36");

// 탐지 우회 옵션
        options.addArguments("--disable-blink-features=AutomationControlled");
//options.addArguments("--headless"); // 가능하면 끄기
        options.addArguments("window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options); // WebDriver 객체 생성

        try {
            driver.get(url);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep((int) ((Math.random() + 0.1) * 300)); // 랜덤 대기 시간 추가 (부하 방지)

            // 원하는 텍스트가 있는 div들을 가져오기
            List<WebElement> textComponents = driver.findElements(By.cssSelector("div.se-component.se-text.se-l-default"));
            System.out.println("텍스트 컴포넌트 개수: " + textComponents.size());

            for (WebElement component : textComponents) {
                List<WebElement> paragraphs = component.findElements(By.cssSelector("p.se-text-paragraph"));
                for (WebElement p : paragraphs) {
                    String text = p.getText().trim();
                    if (!text.isEmpty()) {
                        result.append(text).append("\n"); // 줄바꿈 포함
                    }
                }
                System.out.println("결과 : "+ result);
            }
        } finally {
            driver.quit(); // 리소스 정리
        }
        return new Content(String.valueOf(result));
    }

    /**
     * 블로그 주소 넣으면 본문 반환 (iframe에서 뽑아낸 주소 넣어야함)
     * @param url
     * @return
     * @throws InterruptedException
     */
    public String getNaverBlogContent(String url) throws InterruptedException {
        StringBuilder result = new StringBuilder();

        // Selenium WebDriver 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("window-size=1920,1080");
        // options.addArguments("--headless"); // 필요시 헤드리스 모드

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(1500); // 페이지 로딩 대기

            // iframe이 있는 경우에만 진입
            List<WebElement> iframes = driver.findElements(By.id("mainFrame"));
            if (!iframes.isEmpty()) {
                driver.switchTo().frame("mainFrame");
                System.out.println("✅ iframe 진입 성공");
            } else {
                System.out.println("⚠ iframe 없음 - 최신 블로그 구조로 판단하고 바로 본문 추출 시도");
            }

            // 본문 요소 탐색
            WebElement content;
            try {
                content = driver.findElement(By.cssSelector(".se-main-container")); // 에디터 본문
            } catch (Exception e1) {
                content = driver.findElement(By.cssSelector(".post-view")); // 구형 블로그
            }

            result.append(content.getText().trim());
            System.out.println("✅ 블로그 본문:\n" + result);
//            driver.get(url);
//            Thread.sleep(1000); // 페이지 로딩 대기
//
//            // iframe 내부 진입
//            driver.switchTo().frame("mainFrame");
//
//            // 본문 추출
//            WebElement content = driver.findElement(By.cssSelector(".se-main-container"));
//            result.append(content.getText().trim());
//
//            System.out.println("✅ 블로그 본문:\n" + result);
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 본문 추출 실패: " + e.getMessage();
        } finally {
            driver.quit();
        }

        return result.toString();
    }


    public String getContentBymetaurl(String url) throws IOException, InterruptedException {
        String url2=getMeta(url);
        String content=getNaverBlogContent(url2);
        System.out.println("getContentBymetaurl에서 블로그 글 추출 결과 : "+content);
        return content;
    }


}

