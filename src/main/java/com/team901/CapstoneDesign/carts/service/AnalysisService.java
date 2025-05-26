package com.team901.CapstoneDesign.carts.service;

import com.team901.CapstoneDesign.carts.dto.*;
import com.team901.CapstoneDesign.carts.entity.Analysis;
import com.team901.CapstoneDesign.carts.entity.Cart;
import com.team901.CapstoneDesign.carts.entity.RecommendationResult;
import com.team901.CapstoneDesign.carts.repository.AnalysisRepository;
import com.team901.CapstoneDesign.carts.repository.CartRepository;
import com.team901.CapstoneDesign.global.enums.CartStatus;
import com.team901.CapstoneDesign.mart.dto.MartDetailDto;
import com.team901.CapstoneDesign.product.dto.ProductDetailDto;
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


    public AnalysisResponseDto createAnalysis(AnalysisRequestDto requestDto) {

        // Cart 생성
        Cart cart = new Cart();
        cart.setUserId(requestDto.getUserId());
        cart.setTitle(requestDto.getMemo());
        cart.setStatus(CartStatus.IN_PROGRESS);
        cart.setCreatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Analysis 생성
        Analysis analysis = new Analysis();
        analysis.setCart(cart);
        analysis.setUserId(requestDto.getUserId());
        analysis.setCreatedAt(LocalDateTime.now());
        analysisRepository.save(analysis);

        return new AnalysisResponseDto(
                cart.getCartId(),
                requestDto.getUserId(),
                requestDto.getMemo(),
                CartStatus.IN_PROGRESS.name(),
                cart.getCreatedAt()
        );
    }

    public List<CartSummaryResponseDto> getAllCartsByUser(String userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

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


}
