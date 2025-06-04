package com.team901.CapstoneDesign.carts.service;

import com.team901.CapstoneDesign.carts.dto.*;
import com.team901.CapstoneDesign.carts.entity.Analysis;
import com.team901.CapstoneDesign.carts.entity.Cart;
import com.team901.CapstoneDesign.carts.entity.RecommendationResult;
import com.team901.CapstoneDesign.carts.repository.AnalysisRepository;
import com.team901.CapstoneDesign.carts.repository.CartRepository;
import com.team901.CapstoneDesign.carts.repository.RecommendationResultRepository;
import com.team901.CapstoneDesign.global.enums.CartStatus;
import com.team901.CapstoneDesign.global.enums.MartType;
import com.team901.CapstoneDesign.mart.dto.MartDetailDto;
import com.team901.CapstoneDesign.mart.entity.Mart;
import com.team901.CapstoneDesign.mart.repository.MartRepository;
import com.team901.CapstoneDesign.product.dto.ProductDetailDto;
import com.team901.CapstoneDesign.product.entity.Product;
import com.team901.CapstoneDesign.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final CartRepository cartRepository;
    private final RecommendationResultRepository recommendationResultRepository;
    private final MartRepository martRepository; // 추가
    private final ProductRepository productRepository; // 추가


    public AnalysisResponseDto createAnalysis(AnalysisRequestDto requestDto) {
        Cart cart = new Cart();
        cart.setUserId(requestDto.getUserId());
        cart.setTitle(requestDto.getMemo());
        cart.setStatus(CartStatus.IN_PROGRESS);
        cart.setCreatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        Analysis analysis = new Analysis();
        analysis.setCart(cart);
        analysis.setUserId(requestDto.getUserId());
        analysis.setCreatedAt(LocalDateTime.now());
        analysis.setPriceWeight(0.5);
        analysis.setDistanceWeight(0.5);
        analysis.setIsConfirmed(false); // 초기엔 false
        analysisRepository.save(analysis);

        // 크롤링 데이터 & GPT 결과 기반 최적 추천 로직 호출
        // List<RecommendationResult> results = recommendationEngine.generate(cart, analysis);


        // 임시로 로직 돌아갔다고 가정 후 데이터 넣은 것 !!!!!
        List<RecommendationResult> results = List.of(
                createMockResult(analysis, "우유", "쿠팡", 10000.0, 0.0, 0.0),
                createMockResult(analysis, "당근", "쿠팡", 13400.0, 0.0, 0.0),
                createMockResult(analysis, "파", "이마트", 7000.0, 2.3, null),
                createMockResult(analysis, "마늘", "이마트", 9800.0, 2.3, null)
        );
        recommendationResultRepository.saveAll(results);


        // 연관 관계 세팅
        for (RecommendationResult result : results) {
            result.setAnalysis(analysis);
        }

        // 저장
        recommendationResultRepository.saveAll(results);

        return new AnalysisResponseDto(
                cart.getCartId(),
                requestDto.getUserId(),
                requestDto.getMemo(),
                cart.getStatus().name(),
                cart.getCreatedAt()
        );
    }

    // 임시 분석 생성 mock 데이터 넣기 --> 추후 지울것임
    private RecommendationResult createMockResult(
            Analysis analysis, String productName, String martName, double price, Double distance, Double deliveryFee) {

        Product product = new Product();
        product.setName(productName);
        productRepository.save(product);

        Mart mart = new Mart();
        mart.setName(martName);
        mart.setType(martName.equals("쿠팡") ? MartType.ONLINE : MartType.OFFLINE); // 예시
        martRepository.save(mart);

        RecommendationResult result = new RecommendationResult();
        result.setAnalysis(analysis);
        result.setMart(mart);
        result.setProduct(product);
        result.setTotalPrice(price);
        result.setDistance(distance);
        result.setDeliveryFee(deliveryFee);
        return result;
    }


    public AnalysisResponseDto reanalyze(Long cartId, ReanalyzeRequestDto requestDto) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart Not Found"));
        Analysis analysis = cart.getAnalysis();

        analysis.setPriceWeight(requestDto.getPriceWeight());
        analysis.setDistanceWeight(requestDto.getDistanceWeight());

        // 기존 추천 결과 가져오기
        List<RecommendationResult> results = recommendationResultRepository.findByAnalysis(analysis);

        // 각 상품에 대해 새로운 score = a * price + b * 거리 계산해서 정렬
        for (RecommendationResult result : results) {
            double score = requestDto.getPriceWeight() * result.getTotalPrice()
                    + requestDto.getDistanceWeight() * (result.getDistance() != null ? result.getDistance() : 0);

            result.setScore(score);
        }

        recommendationResultRepository.saveAll(results);
        analysisRepository.save(analysis);

        return new AnalysisResponseDto(
                cart.getCartId(),
                cart.getUserId(),
                cart.getTitle(),
                cart.getStatus().name(),
                cart.getCreatedAt()
        );
    }






    public List<CartSummaryResponseDto> getCartsByUserAndStatus(String userId, CartStatus status) {
        List<Cart> carts;

        if (status == null) {
            carts = cartRepository.findByUserId(userId);
        } else {
            carts = cartRepository.findByUserIdAndStatus(userId, status);
        }

        return carts.stream().map(cart -> {

            // 테스트용 임시 추가 함수 ( 알고리즘 구현 후 삭제 )
            if (cart.getAnalysis() == null || cart.getAnalysis().getRecommendationResults() == null) {
                return new CartSummaryResponseDto(
                        cart.getCartId(),
                        cart.getTitle(),
                        List.of(), // 빈 마트 목록
                        0,
                        0.0,
                        cart.getStatus().name(),
                        cart.getUpdatedAt()
                );
            }

            Map<String, List<RecommendationResult>> groupedByMart = cart.getAnalysis().getRecommendationResults()
                    .stream()
                    .collect(Collectors.groupingBy(result -> result.getMart().getName()));

            List<CartMartSummaryDto> martSummaries = groupedByMart.entrySet().stream()
                    .map(entry -> {
                        String martName = entry.getKey();
                        List<RecommendationResult> results = entry.getValue();
                        Double totalPrice = results.stream()
                                .mapToDouble(RecommendationResult::getTotalPrice)
                                .sum();

                        // 상품 이름만 리스트로
                        List<String> productNames = results.stream()
                                .map(r -> r.getProduct().getName())
                                .collect(Collectors.toList());

                        return new CartMartSummaryDto(martName, productNames, totalPrice);
                    }).collect(Collectors.toList());

            double totalPrice = martSummaries.stream()
                    .mapToDouble(CartMartSummaryDto::getTotalPrice)
                    .sum();

            // totalItems는 0으로 넘기고, 프론트에서 개수 계산
            return new CartSummaryResponseDto(
                    cart.getCartId(),
                    cart.getTitle(),
                    martSummaries,
                    0,
                    totalPrice,
                    cart.getStatus().name(),
                    cart.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }




    public CartDetailResponseDto getCartDetails(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Cart가 존재하지 않습니다."));

        Analysis analysis = cart.getAnalysis();

        //테스트용 임시 추가 함수 ( 알고리즘 구현 후 삭제 )
        if (analysis == null || analysis.getRecommendationResults() == null || analysis.getRecommendationResults().isEmpty()) {
            return new CartDetailResponseDto(0, 0, List.of(), cart.getStatus().name());
        }

        List<MartDetailDto> martDetails = analysis.getRecommendationResults().stream()
                .collect(Collectors.groupingBy(result -> result.getMart().getName()))
                .entrySet().stream()
                .map(entry -> {
                    List<ProductDetailDto> products = entry.getValue().stream()
                            .map(r -> new ProductDetailDto(
                                    r.getProduct().getName(),
                                    r.getTotalPrice(),
                                    r.getPricePer100g()
                            )).collect(Collectors.toList());

                    boolean isOnline = entry.getValue().get(0).getMart().getType().name().equals("ONLINE");
                    Double distance = isOnline ? 0.0 : entry.getValue().get(0).getDistance();
                    String estimatedTime = isOnline ? "0분" : "30분";

                    return new MartDetailDto(
                            entry.getKey(),
                            distance,
                            estimatedTime,
                            products.size(),
                            products.stream().mapToDouble(ProductDetailDto::getPrice).sum(),
                            products
                    );
                }).collect(Collectors.toList());

        int onlineCount = (int) martDetails.stream().filter(m -> m.getDistance() == 0.0).count();
        int offlineCount = martDetails.size() - onlineCount;

        return new CartDetailResponseDto(
                onlineCount,
                offlineCount,
                martDetails,
                cart.getStatus().name());
    }


    public void createCartForTest(CartTestRequestDto requestDto) {
        Cart cart = new Cart();
        cart.setUserId(requestDto.getUserId());
        cart.setTitle(requestDto.getTitle());
        cart.setStatus(CartStatus.IN_PROGRESS);
        cart.setCreatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }


    public ConfirmCartResponseDto confirmCart(Long cartId, String userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 존재 X"));

         if (!cart.getUserId().equals(userId)) {
             throw new IllegalArgumentException("해당 장바구니는 사용자 소유가 아닙니다.");
         }

        cart.setStatus(CartStatus.CONFIRMED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return new ConfirmCartResponseDto(
                cart.getCartId(),
                cart.getStatus().name(),
                cart.getUpdatedAt()
        );
    }


    public CompleteCartResponseDto completeCart(Long cartId, String userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 존재 X"));


         if (!cart.getUserId().equals(userId)) {
             throw new IllegalArgumentException("해당 장바구니는 사용자 소유가 아닙니다.");
         }

        cart.setStatus(CartStatus.COMPLETED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return new CompleteCartResponseDto(
                cart.getCartId(),
                cart.getStatus().name(),
                cart.getUpdatedAt()
        );
    }

    public void deleteCart(Long cartId, String userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 존재 X"));

        if (!cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        if (cart.getStatus() == CartStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("진행중인 장바구니는 삭제할 수 없습니다.");
        }

        cartRepository.delete(cart);
    }

}
