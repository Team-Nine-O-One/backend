package com.team901.CapstoneDesign.GPT.service;




import com.team901.CapstoneDesign.GPT.dto.Product;
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

import java.util.ArrayList;
import java.util.List;

import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

@Service
public class SeleniumNaverShoppingCrawler {
    public List<Product> getShoppingData(String keyword, int page) throws InterruptedException {
        // 크롤링 결과를 담을 리스트 초기화
        List<Product> result = new ArrayList<>();
        int num = 1; // 상품 번호 초기화

        // Selenium WebDriver 설정
        ChromeOptions options = new ChromeOptions(); // 크롬 옵션 객체 생성
        WebDriver driver = new ChromeDriver(options); // WebDriver 객체 생성

        try {

            // 네이버 쇼핑 검색 URL 생성
            String url = String.format(
                    "https://search.shopping.naver.com/search/all?pagingIndex=%d&query=%s", page, keyword);
            driver.get(url); // URL로 이동

            // 페이지 끝까지 스크롤
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep((int) ((Math.random() + 0.1) * 300)); // 랜덤 대기 시간 추가 (부하 방지)

            // 상품 리스트 크롤링
            List<WebElement> productElements = driver.findElements(By.cssSelector(".basicList_list_basis__XVx_G > div > div"));

            System.out.println("크롤링할 상품 개수: " + productElements.size());
            for (WebElement element : productElements) {
                // 상품 HTML 파싱 및 Product 객체 생성
                Product product = parseProduct(num, element.getAttribute("innerHTML"));
                System.out.println("원시 HTML: " + html);
                if (product != null) {
                    result.add(product); // 결과 리스트에 추가
                    System.out.println("✅ 파싱 성공: " + product);
                    num++; // 상품 번호 증가
                } else {
                    System.out.println("❌ 파싱 실패");
                }
            }

        } finally {
            driver.quit(); // 브라우저 종료
        }

        return result; // 크롤링 결과 반환
    }


    /**
     * HTML을 파싱하여 Product 객체 생성
     *
     * @param num  상품 번호
     * @param html 상품 정보를 담고 있는 HTML
     * @return Product 객체
     */
    private Product parseProduct(int num, String html) {
        // Jsoup을 사용해 HTML 파싱
        Document doc = Jsoup.parse(html);

        // 상품 제목, 가격, 쇼핑몰 정보 추출
        String title = extractTitle(doc); // 상품 제목 추출
        String price = extractPrice(doc); // 상품 가격 추출
        //String mall = extractMall(doc);   // 쇼핑몰 정보 추출

//        // 모든 정보가 유효한 경우에만 Product 객체 생성
//        if (title == null || price == null || mall == null) return null;
//        System.out.println("num= " + num + " ,title= " + title + " ,mall= " + mall + " ,price= " + price);
//        return new Product(num, title, mall, price);

        // 모든 정보가 유효한 경우에만 Product 객체 생성
        if (title == null || price == null ) return null;
        System.out.println("num= " + num + " ,title= " + title +  " ,price= " + price);
        return new Product(num, title, price);
    }

    /**
     * 상품 제목 추출
     *
     * @param doc Jsoup으로 파싱된 HTML 문서
     * @return 상품 제목 또는 null
     */
    private String extractTitle(Document doc) {
        // 상품 제목에 해당하는 HTML 요소 선택
        Elements titleElements = doc.select(
                "a.product_link__aFnaq, a.adProduct_link__hNwpz");
        // title 속성이 비어 있으면 텍스트를 반환
        return !titleElements.isEmpty() ? titleElements.attr("title").isEmpty()
                ? titleElements.text()
                : titleElements.attr("title") : null;
    }

    /**
     * 상품 가격 추출
     *
     * @param doc Jsoup으로 파싱된 HTML 문서
     * @return 상품 가격 또는 null
     */
    private String extractPrice(Document doc) {
        // 상품 가격에 해당하는 HTML 요소 선택
        Elements priceElements = doc.select(
                "span.price_num__Y66T7 em");
        // 가격 텍스트 반환
        return !priceElements.isEmpty() ? priceElements.text() : null;
    }

    /**
     * 쇼핑몰 정보 추출
     *
     * @param doc Jsoup으로 파싱된 HTML 문서
     * @return 쇼핑몰 정보 또는 null
     */
    private String extractMall(Document doc) {
        // Case 1: 텍스트 기반 쇼핑몰 정보 추출
        Elements mallElements = doc.select(
                "a.product_mall__hPiEH, a.superSavingProduct_mall__dEYF7, a.adProduct_mall__zeLIC");
        if (!mallElements.isEmpty()) {
            // title 속성이 비어 있으면 텍스트 반환
            String mall = mallElements.attr("title").isEmpty() ? mallElements.text() : mallElements.attr("title");
            if (mall != null && !mall.isEmpty()) {
                return mall;
            }
        }

        // Case 2: 이미지 태그의 alt 속성 추출
        Elements mallImgElements = doc.select(
                "a.product_mall__hPiEH img, a.superSavingProduct_mall__dEYF7 img, a.adProduct_mall__zeLIC img");
        if (!mallImgElements.isEmpty()) {
            // 이미지 태그의 alt 속성 반환
            return mallImgElements.attr("alt");
        }

        // Case 3: Subcase 처리 (리스트 형태 쇼핑몰 정보)
        Elements mallListElements = doc.select("span.product_mall_name__MbUf3");
        if (!mallListElements.isEmpty()) {
            // 여러 쇼핑몰 이름을 쉼표로 연결
            List<String> mallList = new ArrayList<>();
            mallListElements.forEach(mall -> mallList.add(mall.text()));
            return String.join(", ", mallList);
        }

        // 정보가 없는 경우 null 반환
        return null;
    }

}