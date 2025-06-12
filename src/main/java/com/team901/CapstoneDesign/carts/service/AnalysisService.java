package com.team901.CapstoneDesign.carts.service;

import com.team901.CapstoneDesign.carts.dto.*;
import com.team901.CapstoneDesign.entity.Market;
import com.team901.CapstoneDesign.entity.Memo;
import com.team901.CapstoneDesign.repository.MemoItemRepository;
import com.team901.CapstoneDesign.repository.MemoRepository;
import com.team901.CapstoneDesign.service.MemoService;
import com.team901.CapstoneDesign.carts.entity.Analysis;
import com.team901.CapstoneDesign.carts.entity.Cart;
import com.team901.CapstoneDesign.carts.entity.RecommendationResult;
import com.team901.CapstoneDesign.carts.repository.AnalysisRepository;
import com.team901.CapstoneDesign.carts.repository.CartRepository;
import com.team901.CapstoneDesign.carts.repository.RecommendationResultRepository;
import com.team901.CapstoneDesign.dto.MemoRequestDTO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final CartRepository cartRepository;
    private final RecommendationResultRepository recommendationResultRepository;
    private final MartRepository martRepository;
    private final MemoService memoService;
    private final MemoRepository memoRepository;


    private static final List<String> PRICE_PRIORITY_ROUTE = Arrays.asList(
            "이마트에브리데이 흑석동점",  // market_id = 1
            "홈플러스익스프레스 상도2점", // market_id = 2
            "GS더프레시 동작상도점",     // market_id = 3
            "하나로마트 흑석점"          // market_id = 4
    );

    private static final List<String> DISTANCE_PRIORITY_ROUTE = Arrays.asList(
            "이마트에브리데이 흑석동점",  // market_id = 1
            "홈플러스익스프레스 상도2점"  // market_id = 2
    );

    private static final List<String> OPTIMAL_ROUTE = Arrays.asList(
            "이마트에브리데이 흑석동점",  // market_id = 1
            "홈플러스익스프레스 상도2점", // market_id = 2
            "GS더프레시 동작상도점"      // market_id = 3
    );


    public AnalysisResponseDto createAnalysis(AnalysisRequestDto requestDto) {
        // 1. Memo 조회
        Memo memo = memoRepository.findById(requestDto.getMemoId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메모가 존재하지 않습니다."));

        // 2. Cart 생성
        Cart cart = new Cart();
        cart.setUserId(requestDto.getUserId());
        cart.setTitle(memo.getRawText());
        cart.setStatus(CartStatus.IN_PROGRESS);
        cart.setCreatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // 3. Analysis 생성
        Analysis analysis = new Analysis();
        analysis.setCart(cart);
        analysis.setUserId(requestDto.getUserId());
        analysis.setCreatedAt(LocalDateTime.now());
        analysis.setPriceWeight(0.5);
        analysis.setDistanceWeight(0.5);
        analysis.setIsConfirmed(false);
        analysis.setUserLatitude(memo.getUserLat());
        analysis.setUserLongitude(memo.getUserLng());

        analysisRepository.save(analysis);

        // 4. Memo → Analysis로 결과 연결
        memoService.generateOptimizedMarketCartsAndBindToAnalysis(memo, analysis);

        return new AnalysisResponseDto(
                cart.getCartId(),
                cart.getUserId(),
                cart.getTitle(),
                cart.getStatus().name(),
                cart.getCreatedAt()
        );
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






    public List<CartSummaryResponseDto> getCartsByUserAndStatus(String userId, CartStatus status) { // ⭐ 여기에 String priority 파라미터가 누락되었습니다!

        List<Cart> carts;

        if (status == null) {
            carts = cartRepository.findByUserId(userId);
        } else {
            carts = cartRepository.findByUserIdAndStatus(userId, status);
        }

        return carts.stream().map(cart -> {

            Map<String, List<RecommendationResult>> groupedByMart = cart.getAnalysis().getRecommendationResults()
                    .stream()
                    .collect(Collectors.groupingBy(result -> result.getMart().getName()));

            List<CartMartSummaryDto> martSummaries = groupedByMart.entrySet().stream()
                    .map(entry -> {
                        String martName = entry.getKey();
                        List<RecommendationResult> results = entry.getValue();
                        Double currentTotalPrice = results.stream()
                                .mapToDouble(RecommendationResult::getTotalPrice)
                                .sum();

                        List<String> productNames = results.stream()
                                .filter(r -> r.getProduct() != null)
                                .map(r -> r.getProduct().getName())
                                .collect(Collectors.toList());

                        return new CartMartSummaryDto(martName, productNames, currentTotalPrice);
                    }).collect(Collectors.toList());

            Double finalTotalPrice = martSummaries.stream()
                    .mapToDouble(CartMartSummaryDto::getTotalPrice)
                    .sum();

            // totalItems는 0으로 넘기고, 프론트에서 개수 계산
            return new CartSummaryResponseDto(
                    cart.getCartId(),
                    cart.getTitle(),
                    martSummaries,
                    0,
                    finalTotalPrice,
                    cart.getStatus().name(),
                    cart.getUpdatedAt(),
                    cart.getStatus() == CartStatus.COMPLETED,
                    OPTIMAL_ROUTE, // 최적 루트
                    DISTANCE_PRIORITY_ROUTE, // 거리 우선 루트
                    PRICE_PRIORITY_ROUTE // 가격 우선 루트
            );
        }).collect(Collectors.toList());
    }




//    public CartDetailResponseDto getCartDetails(Long cartId) {
//        Cart cart = cartRepository.findById(cartId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 Cart가 존재하지 않습니다."));
//
//        Analysis analysis = cart.getAnalysis();
//
//
//        List<MartDetailDto> martDetails = analysis.getRecommendationResults().stream()
//                .collect(Collectors.groupingBy(result -> result.getMart().getName()))
//                .entrySet().stream()
//                .map(entry -> {
//
//                    var firstResult = entry.getValue().get(0);
//                    var mart = firstResult.getMart();
//
//                    List<ProductDetailDto> products = entry.getValue().stream()
//                            .map(r -> new ProductDetailDto(
//                                    r.getProduct().getName(),
//                                    r.getTotalPrice(),
//                                    r.getPricePer100g()
//                            )).collect(Collectors.toList());
//
//                    boolean isOnline = entry.getValue().get(0).getMart().getType().name().equals("ONLINE");
//                    Double distance = isOnline ? 0.0 : entry.getValue().get(0).getDistance();
//                    String estimatedTime = isOnline ? "0분" : "30분";
//
//                    return new MartDetailDto(
//                            entry.getKey(),
//                            distance,
//                            estimatedTime,
//                            products.size(),
//                            products.stream().mapToDouble(ProductDetailDto::getPrice).sum(),
//                            products,
//                            mart.getLatitude(),
//                            mart.getLongitude()
//                    );
//                }).collect(Collectors.toList());
//
//        int onlineCount = (int) martDetails.stream().filter(m -> m.getDistance() == 0.0).count();
//        int offlineCount = martDetails.size() - onlineCount;
//
//        return new CartDetailResponseDto(
//                onlineCount,
//                offlineCount,
//                martDetails,
//                cart.getStatus().name());
//    }


    public CartDetailGroupedResponseDto getCartDetails(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Cart가 존재하지 않습니다."));

        Analysis analysis = cart.getAnalysis();
        List<RecommendationResult> results = analysis.getRecommendationResults();

        List<RecommendationResult> onlineResults = results.stream()
                .filter(r -> r.getMart().getType() == MartType.ONLINE)
                .collect(Collectors.toList());

        List<RecommendationResult> offlineResults = results.stream()
                .filter(r -> r.getMart().getType() == MartType.OFFLINE)
                .collect(Collectors.toList());

        // 온라인 마트
        List<ProductDetailDto> onlineProducts = onlineResults.stream()
                .map(r -> new ProductDetailDto(
                        r.getProduct().getName(),
                        r.getTotalPrice(),
                        r.getPricePer100g()
                )).collect(Collectors.toList());

        double onlineTotalPrice = onlineProducts.stream().mapToDouble(ProductDetailDto::getPrice).sum();
        OnlineMartDto onlineDto = new OnlineMartDto(
                onlineProducts.size(),
                onlineTotalPrice,
                onlineProducts
        );

        // 오프라인 마트 루트별 분류
        Map<String, List<RecommendationResult>> groupedByMartName = offlineResults.stream()
                .collect(Collectors.groupingBy(r -> r.getMart().getName()));

        List<MartDetailDto> optimal = OPTIMAL_ROUTE.stream()
                .filter(groupedByMartName::containsKey)
                .map(name -> toMartDetail(groupedByMartName.get(name)))
                .collect(Collectors.toList());

        List<MartDetailDto> distance = DISTANCE_PRIORITY_ROUTE.stream()
                .filter(groupedByMartName::containsKey)
                .map(name -> toMartDetail(groupedByMartName.get(name)))
                .collect(Collectors.toList());

        List<MartDetailDto> price = PRICE_PRIORITY_ROUTE.stream()
                .filter(groupedByMartName::containsKey)
                .map(name -> toMartDetail(groupedByMartName.get(name)))
                .collect(Collectors.toList());

        return new CartDetailGroupedResponseDto(
                onlineDto,
                new OfflineMartGroupDto(optimal, distance, price),
                cart.getStatus().name()
        );
    }

    private MartDetailDto toMartDetail(List<RecommendationResult> results) {
        RecommendationResult first = results.get(0);
        Market mart = first.getMart();
        boolean isOnline = mart.getType() == MartType.ONLINE;
        double distance = isOnline ? 0.0 : first.getDistance();
        String estimatedTime = isOnline ? "0분" : "30분";

        List<ProductDetailDto> products = results.stream()
                .map(r -> new ProductDetailDto(
                        r.getProduct().getName(),
                        r.getTotalPrice(),
                        r.getPricePer100g()
                )).collect(Collectors.toList());

        return new MartDetailDto(
                mart.getName(),
                distance,
                estimatedTime,
                products.size(),
                products.stream().mapToDouble(ProductDetailDto::getPrice).sum(),
                products,
                mart.getLatitude(),
                mart.getLongitude()
        );
    }



    public ConfirmCartResponseDto confirmCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 존재 X"));

        cart.setStatus(CartStatus.CONFIRMED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return new ConfirmCartResponseDto(
                cart.getCartId(),
                cart.getStatus().name(),
                cart.getUpdatedAt()
        );
    }


    public CompleteCartResponseDto completeCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 존재 X"));

        cart.setStatus(CartStatus.COMPLETED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return new CompleteCartResponseDto(
                cart.getCartId(),
                cart.getStatus().name(),
                cart.getUpdatedAt()
        );
    }

    public void deleteCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 존재 X"));

        if (cart.getStatus() == CartStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("진행중인 장바구니는 삭제할 수 없습니다.");
        }

        cartRepository.delete(cart);
    }


    public GroupedCartDetailResponseDto getGroupedCartDetails(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Cart가 존재하지 않습니다."));

        Analysis analysis = cart.getAnalysis();

        List<RecommendationResult> results = analysis.getRecommendationResults();

        // 온라인 (쿠팡, mart_id = 5)
        List<RecommendationResult> onlineResults = results.stream()
                .filter(r -> r.getMart().getId() == 5)
                .toList();

        List<ProductDetailDto> onlineProducts = onlineResults.stream()
                .map(r -> new ProductDetailDto(
                        r.getProduct().getName(),
                        r.getTotalPrice(),
                        r.getPricePer100g()
                )).toList();

        OnlineMartDto onlineMartDto = new OnlineMartDto(
                onlineProducts.size(),
                onlineProducts.stream().mapToDouble(ProductDetailDto::getPrice).sum(),
                onlineProducts
        );

        // 오프라인: 마트 이름 기준 그룹핑
        Map<String, List<RecommendationResult>> offlineGrouped = results.stream()
                .filter(r -> r.getMart().getType() == MartType.OFFLINE)
                .collect(Collectors.groupingBy(r -> r.getMart().getName()));

        List<MartDetailDto> offlineMartDtos = offlineGrouped.entrySet().stream()
                .map(entry -> {
                    List<RecommendationResult> martResults = entry.getValue();
                    Market mart = martResults.get(0).getMart();


                    List<ProductDetailDto> products = martResults.stream()
                            .map(r -> new ProductDetailDto(
                                    r.getProduct().getName(),
                                    r.getTotalPrice(),
                                    r.getPricePer100g()
                            )).toList();

                    return new MartDetailDto(
                            mart.getName(),
                            martResults.get(0).getDistance(),
                            "30분",
                            products.size(),
                            products.stream().mapToDouble(ProductDetailDto::getPrice).sum(),
                            products,
                            mart.getLatitude(),
                            mart.getLongitude()
                    );
                }).toList();

        return new GroupedCartDetailResponseDto(
                onlineMartDto,
                offlineMartDtos,
                cart.getStatus().name()
        );
    }

}
