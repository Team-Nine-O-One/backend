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
import java.util.*;
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


    public AnalysisResponseDto createAnalysis(AnalysisRequestDto requestDto) {

        String fixedUserId = "1";

        // 1. Memo 조회
        Memo memo = memoRepository.findById(requestDto.getMemoId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메모가 존재하지 않습니다."));

        // 2. Cart 생성
        Cart cart = new Cart();
        cart.setUserId(fixedUserId);
        cart.setTitle(memo.getRawText());
        cart.setStatus(CartStatus.IN_PROGRESS);
        cart.setCreatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // 3. Analysis 생성
        Analysis analysis = new Analysis();
        analysis.setCart(cart);
        analysis.setUserId(fixedUserId);
        analysis.setCreatedAt(LocalDateTime.now());
        analysis.setPriceWeight(0.5);
        analysis.setDistanceWeight(0.5);
        analysis.setIsConfirmed(false);
        analysis.setUserLatitude(memo.getUserLat());
        analysis.setUserLongitude(memo.getUserLng());

        analysisRepository.save(analysis);

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
            System.out.println("전달할 userId: " + userId);
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

            int totalItems = martSummaries.stream()
                    .mapToInt(ms -> ms.getProductNames().size())
                    .sum();


            // totalItems는 0으로 넘기고, 프론트에서 개수 계산
            return new CartSummaryResponseDto(
                    cart.getCartId(),
                    cart.getTitle(),
                    martSummaries,
                    totalItems,
                    finalTotalPrice,
                    cart.getStatus().name(),
                    cart.getUpdatedAt(),
                    cart.getStatus() == CartStatus.COMPLETED,
                    Collections.emptyList(), // OPTIMAL_ROUTE
                    Collections.emptyList(), // DISTANCE_PRIORITY_ROUTE
                    Collections.emptyList()  // PRICE_PRIORITY_ROUTE
            );
        }).collect(Collectors.toList());
    }


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

        Map<String, List<RecommendationResult>> groupedByMartName = offlineResults.stream()
                .collect(Collectors.groupingBy(r -> r.getMart().getName()));

        // 온라인 마트
        List<ProductDetailDto> onlineProducts = onlineResults.stream()
                .map(r -> new ProductDetailDto(
                        r.getProduct().getName(),
                        r.getTotalPrice(),
                        r.getPricePer100g()
                )).collect(Collectors.toList());

        String onlineMartName = onlineResults.isEmpty() ? "온라인마트" : onlineResults.get(0).getMart().getName();

        double onlineTotalPrice = onlineProducts.stream().mapToDouble(ProductDetailDto::getPrice).sum();
        OnlineMartDto onlineDto = new OnlineMartDto(
                onlineMartName,
                onlineProducts.size(),
                onlineTotalPrice,
                onlineProducts
        );

        // 오프라인 마트 루트별 분류
//        Map<String, List<RecommendationResult>> groupedByMartName = offlineResults.stream()
//                .collect(Collectors.groupingBy(r -> r.getMart().getName()));

        List<MartDetailDto> priceSorted = groupedByMartName.values().stream()
                .map(this::toMartDetail)
                .sorted(Comparator.comparingDouble(MartDetailDto::getTotalPrice))
                .collect(Collectors.toList());

        List<MartDetailDto> distanceSorted = groupedByMartName.values().stream()
                .map(this::toMartDetail)
                .sorted(Comparator.comparingDouble(MartDetailDto::getDistance))
                .collect(Collectors.toList());

        List<MartDetailDto> noPriority = groupedByMartName.values().stream()
                .map(this::toMartDetail)
                .collect(Collectors.toList());

        return new CartDetailGroupedResponseDto(
                onlineDto,
                new OfflineMartGroupDto(
                        noPriority,        // "최적" 혹은 기본
                        distanceSorted,
                        priceSorted
                ),
                cart.getStatus().name()
        );

    }

    private MartDetailDto toMartDetail(List<RecommendationResult> results) {
        RecommendationResult first = results.get(0);
        Market mart = first.getMart();
        boolean isOnline = mart.getType() == MartType.ONLINE;

        double distance = calculateDistance(
                37.5025, 126.9822, // 중앙대 310관
                mart.getLatitude(), mart.getLongitude()
        );

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



    // 하드코딩 계산
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km 단위 지구 반지름
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0; // km, 소수점 2자리까지
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

        String onlineMartName = onlineResults.isEmpty() ? "온라인 마트" : onlineResults.get(0).getMart().getName();

        OnlineMartDto onlineMartDto = new OnlineMartDto(
                onlineMartName,
                onlineProducts.size(),
                onlineProducts.stream().mapToDouble(ProductDetailDto::getPrice).sum(),
                onlineProducts
        );

        // 오프라인: 마트 이름 기준 그룹핑
        Map<String, List<RecommendationResult>> offlineGrouped = results.stream()
                .filter(r -> r.getMart().getType() == MartType.OFFLINE)
                .collect(Collectors.groupingBy(r -> r.getMart().getName()));

        List<MartDetailDto> allOffline = offlineGrouped.values().stream()
                .map(this::toMartDetail)
                .toList();

        List<MartDetailDto> optimal = new ArrayList<>(allOffline); // 최적 경로: 그냥 전체
        List<MartDetailDto> distance = allOffline.stream()
                .sorted(Comparator.comparingDouble(MartDetailDto::getDistance))
                .toList();

        List<MartDetailDto> price = allOffline.stream()
                .sorted(Comparator.comparingDouble(MartDetailDto::getTotalPrice))
                .toList();

        return new GroupedCartDetailResponseDto(
                onlineMartDto,
                new RouteGroupedOfflineDto("최적 경로", optimal),
                new RouteGroupedOfflineDto("거리 우선 경로", distance),
                new RouteGroupedOfflineDto("가격 우선 경로", price),
                cart.getStatus().name(),
                optimal.stream().map(MartDetailDto::getMartName).toList(),
                distance.stream().map(MartDetailDto::getMartName).toList(),
                price.stream().map(MartDetailDto::getMartName).toList()
        );

    }

}
