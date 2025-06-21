package com.team901.CapstoneDesign.service;

import com.team901.CapstoneDesign.GPT.service.GPTService;
import com.team901.CapstoneDesign.carts.entity.Analysis;
import com.team901.CapstoneDesign.carts.entity.RecommendationResult;
import com.team901.CapstoneDesign.carts.repository.RecommendationResultRepository;
import com.team901.CapstoneDesign.dto.BestOptimizedResultDTO;
import com.team901.CapstoneDesign.dto.MarketCartResponseDTO;
import com.team901.CapstoneDesign.dto.MemoRequestDTO;
import com.team901.CapstoneDesign.entity.*;
import com.team901.CapstoneDesign.mart.entity.Mart;
import com.team901.CapstoneDesign.global.enums.MartType;
import com.team901.CapstoneDesign.product.entity.Product;
import com.team901.CapstoneDesign.product.repository.ProductRepository;
import com.team901.CapstoneDesign.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final GPTService gptService;

    @Autowired
    private MemoRepository memoRepo;
    @Autowired private MemoItemRepository memoItemRepo;
    @Autowired private ProductsRepository productsRepo;
    @Autowired private MarketCartRepository marketCartRepo;
    @Autowired private MarketCartItemRepository marketCartItemRepo;
    @Autowired private MarketRepository marketRepo;
    @Autowired private MemoItemProductRepository memoItemProductRepository;
    @Autowired private OptimizedResultRepository optimizedResultRepository;
    @Autowired private OptimizedResultItemRepository optimizedResultItemRepository;
    @Autowired private ScoreRepository scoreRepository;
    @Autowired
    private OfflineCombinationResultRepository offlineCombinationResultRepository;
    private final DistanceMatrixService distanceMatrixService;

    @Autowired
    private RecommendationResultRepository recommendationResultRepository;

    @Autowired private ProductRepository productRepository;

    public Memo createMemoWithItems(MemoRequestDTO dto) {
        Memo memo = new Memo();
        memo.setRawText(dto.rawText);
        memo.setUserId("1");
        memo.setUserLat(dto.userLat);
        memo.setUserLng(dto.userLng);
        memo.setCreatedAt(new Date());

        List<MemoItem> memoItems = new ArrayList<>();

        String[] items = dto.rawText.split("\\|");
        for (String itemText : items) {
            String[] split = itemText.trim().split(" ");
            if (split.length >= 2) {
                MemoItem item = new MemoItem();
                item.setName(split[0]);
                item.setQuantity(split[1]);
                item.setMemo(memo);
                memoItems.add(item);
            }
        }

        memo.setMemoItems(memoItems); // 양방향 연관관계 연결
        memoRepo.save(memo); // ✅ cascade = ALL 이면 MemoItem 도 같이 저장됨
        return memo;
    }

    public List<MarketCartResponseDTO> generateMarketCarts(Long memoId, Double userLat, Double userLng) {
        Memo memo = memoRepo.findById(memoId).orElseThrow();
        List<Market> markets = marketRepo.findAll();

        List<MarketCartResponseDTO> result = new ArrayList<>();

        for (Market market : markets) {
            MarketCart cart = new MarketCart();
            cart.setMemo(memo);
            cart.setMarket(market);
            cart.setUserLat(userLat);
            cart.setUserLng(userLng);

            if (market.getType() == MartType.OFFLINE) {
                cart.setDistanceFromUser(HaversineUtil.distance(userLat, userLng, market.getLatitude(), market.getLongitude()));
            }

            int total = 0;
            List<MarketCartResponseDTO.CartItemDTO> items = new ArrayList<>();

            for (MemoItem item : memo.getMemoItems()) {
                List<Products> candidates = productsRepo.findByNameContainingIgnoreCase(item.getName());
                candidates.removeIf(p -> !p.getMarket().getId().equals(market.getId()));
                Products cheapest = candidates.stream().min(Comparator.comparingInt(Products::getPrice)).orElse(null);

                if (cheapest != null) {
                    MarketCartItem cartItem = new MarketCartItem();
                    cartItem.setMarketCart(cart);
                    cartItem.setMemoItem(item);
                    cartItem.setProducts(cheapest);
                    cartItem.setPrice(cheapest.getPrice());
                    marketCartItemRepo.save(cartItem);

                    MarketCartResponseDTO.CartItemDTO dtoItem = new MarketCartResponseDTO.CartItemDTO();
                    dtoItem.memoItemName = item.getName();
                    dtoItem.productName = cheapest.getName();
                    dtoItem.price = cheapest.getPrice();
                    items.add(dtoItem);

                    total += cheapest.getPrice();
                }
            }

            cart.setTotalPrice(total);
            marketCartRepo.save(cart);

            MarketCartResponseDTO response = new MarketCartResponseDTO();
            response.marketName = market.getName();
            response.totalPrice = total;
            response.distanceFromUser = cart.getDistanceFromUser();
            response.items = items;
            result.add(response);
        }

        return result;
    }



    public void recommendProductsForMemoItem(MemoItem memoItem) {
        List<Products> candidates = productsRepo.findByNameContainingIgnoreCase(memoItem.getName());

        System.out.println("🧠 MemoItem: " + memoItem.getName() + ", candidates.size = " + candidates.size());
        if (candidates.isEmpty()) {
            System.out.println("❌ 후보 상품 없음. 메모 아이템: " + memoItem.getName());
            return;}

        StringBuilder prompt = new StringBuilder();
        for (Products product : candidates) {
            prompt.append(String.format("상품명: %s, 용량: %s, 가격: %d원, 카테고리: %s\n",
                    product.getName(), product.getVolumeInfo(), product.getPrice(), product.getCategory()));
        }
        prompt.append("\n");
        prompt.append("메모 내용: ").append(memoItem.getName()).append(" ").append(memoItem.getQuantity()).append("\n");
        prompt.append("위의 상품 중에서 가장 적절한 상품을 하나만 골라주세요. 이름만 반환하세요.");

        String promptStr = prompt.toString();
        System.out.println("📨 GPT 요청 프롬프트:\n" + promptStr);

        String gptResult = gptService.getChatResponse(prompt.toString());
        System.out.println("📩 GPT 응답:\n" + gptResult);

        List<String> gptSelected = Arrays.stream(gptResult.split("\n"))
                .map(s -> s.replaceAll("^[0-9]+[\\.|\\)]\\s*", "").trim()) // 번호 제거
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<Products> matched = candidates.stream()
                .filter(p -> gptSelected.stream().anyMatch(sel -> p.getName().contains(sel)))
                .collect(Collectors.toList());

        System.out.println("🎯 GPT가 선택한 상품 개수: " + matched.size());

        for (Products p : matched) {
            MemoItemProduct mip = new MemoItemProduct();
            mip.setMemoItem(memoItem);
            mip.setProducts(p);
            mip.setRecommended(true);
            mip.setCheapest(false);
            memoItemProductRepository.save(mip);
            System.out.println("✅ 저장된 추천상품: " + p.getName());
        }
    }


    public void recommendForMemo(Memo memo) {
        for (MemoItem item : memo.getMemoItems()) {
            recommendProductsForMemoItem(item);
        }
    }


    public List<MarketCartResponseDTO> generateOptimizedMarketCarts(Long memoId, Double userLat, Double userLng) {
        Memo memo = memoRepo.findByIdWithItems(memoId).orElseThrow();
        System.out.println("🔍 최적화 장바구니 생성 시작 - Memo ID: " + memoId);
        System.out.println("📦 포함된 MemoItems 수: " + memo.getMemoItems().size());
        recommendForMemo(memo); // GPT 추천 먼저 실행

        List<Market> markets = marketRepo.findAll();
        List<MarketCartResponseDTO> result = new ArrayList<>();

        for (Market market : markets) {
            System.out.println("🛒 마트 이름: " + market.getName());
            MarketCart cart = new MarketCart();
            cart.setMemo(memo);
            cart.setMarket(market);
            cart.setUserLat(userLat);
            cart.setUserLng(userLng);

            if (market.getType() == MartType.OFFLINE) {
                cart.setDistanceFromUser(HaversineUtil.distance(userLat, userLng, market.getLatitude(), market.getLongitude()));
                System.out.println("📍 거리 계산됨: " + cart.getDistanceFromUser());
            }

            marketCartRepo.save(cart);

            int total = 0;
            List<MarketCartResponseDTO.CartItemDTO> items = new ArrayList<>();

            for (MemoItem item : memo.getMemoItems()) {
                System.out.println("🔍 메모 아이템: " + item.getName() + " " + item.getQuantity());
                // GPT 추천 상품 중 현재 마트에 있는 상품만 필터링
                List<MemoItemProduct> recommended = memoItemProductRepository.findByMemoItemAndRecommendedTrue(item).stream()
                        .filter(p -> p.getProducts().getMarket().getId().equals(market.getId()))
                        .toList();
                System.out.println("➡️ " + market.getName() + "에 존재하는 추천 상품 수: " + recommended.size());
                if (recommended.isEmpty()) continue;

                // 최저가 선택
                MemoItemProduct cheapest = recommended.stream()
                        .min(Comparator.comparingInt(p -> p.getProducts().getPrice()))
                        .orElse(null);

                if (cheapest != null) {
                    System.out.println("💰 선택된 상품: " + cheapest.getProducts().getName() + " / 가격: " + cheapest.getProducts().getPrice());
                    cheapest.setCheapest(true);
                    memoItemProductRepository.save(cheapest);

                    MarketCartItem cartItem = new MarketCartItem();
                    cartItem.setMarketCart(cart);
                    cartItem.setMemoItem(item);
                    cartItem.setProducts(cheapest.getProducts());
                    cartItem.setPrice(cheapest.getProducts().getPrice());
                    marketCartItemRepo.save(cartItem);

                    MarketCartResponseDTO.CartItemDTO dtoItem = new MarketCartResponseDTO.CartItemDTO();
                    dtoItem.memoItemName = item.getName();
                    dtoItem.productName = cheapest.getProducts().getName();
                    dtoItem.price = cheapest.getProducts().getPrice();
                    items.add(dtoItem);

                    total += cheapest.getProducts().getPrice();
                }
            }

            cart.setTotalPrice(total);
            marketCartRepo.save(cart);
            System.out.println("✅ 최종 장바구니 - 마트: " + market.getName() + ", 총액: " + total + ", 상품 수: " + items.size());

            MarketCartResponseDTO response = new MarketCartResponseDTO();
            response.marketName = market.getName();
            response.totalPrice = total;
            response.distanceFromUser = cart.getDistanceFromUser();
            response.items = items;
            result.add(response);

            OptimizedResult optimizedResult = new OptimizedResult();
            optimizedResult.setMemo(memo);
            optimizedResult.setMarketName(market.getName());
            optimizedResult.setTotalPrice(total);
            optimizedResult.setDistance(cart.getDistanceFromUser());
            optimizedResultRepository.save(optimizedResult);


            for (MarketCartResponseDTO.CartItemDTO dtoItem : items) {
                OptimizedResultItem item = new OptimizedResultItem();
                item.setOptimizedResult(optimizedResult);
                item.setMemoItemName(dtoItem.memoItemName);
                item.setProductName(dtoItem.productName);
                item.setPrice(dtoItem.price);
                // ✅ 양방향 연관관계 연결
                optimizedResult.getItems().add(item);

                optimizedResultItemRepository.save(item);
            }

        }

        return result;
    }


    public void calculateScoresForMemo(Long memoId, double priceWeight, double distanceWeight) {
        Memo memo = memoRepo.findById(memoId).orElseThrow();
        List<OptimizedResult> results = optimizedResultRepository.findByMemo(memo);

        if (results.isEmpty()) return;

        // 가격/거리 추출
        double maxPrice = results.stream().mapToDouble(OptimizedResult::getTotalPrice).max().orElse(1);
        double minPrice = results.stream().mapToDouble(OptimizedResult::getTotalPrice).min().orElse(0);
        double maxDist = results.stream().mapToDouble(r -> r.getDistance() != null ? r.getDistance() : 0).max().orElse(1);
        double minDist = results.stream().mapToDouble(r -> r.getDistance() != null ? r.getDistance() : 0).min().orElse(0);

        for (OptimizedResult result : results) {
            double priceNorm = (maxPrice == minPrice) ? 1.0 : 1.0 - (result.getTotalPrice() - minPrice) / (maxPrice - minPrice);
            double distanceNorm = (maxDist == minDist) ? 1.0 : 1.0 - ((result.getDistance() != null ? result.getDistance() : 0.0) - minDist) / (maxDist - minDist);

            double finalScore = priceNorm * priceWeight + distanceNorm * distanceWeight;

            Score score = new Score();
            score.setMemo(memo);
            score.setMarketName(result.getMarketName());
            score.setScore(finalScore);
            scoreRepository.save(score);

            System.out.printf("🧮 점수 저장: 마트=%s, 가격정규화=%.2f, 거리정규화=%.2f, 최종점수=%.2f%n",
                    result.getMarketName(), priceNorm, distanceNorm, finalScore);
        }
    }

    public BestOptimizedResultDTO getBestOptimizedResultForMemo(Long memoId) {
        // 최고 점수 Score 조회
        Score topScore = scoreRepository.findTopByMemoIdOrderByScoreDesc(memoId)
                .orElseThrow(() -> new RuntimeException("해당 메모의 점수 정보가 없습니다."));

        // OptimizedResult 조회
        OptimizedResult result = optimizedResultRepository.findByMemoIdAndMarketName(memoId, topScore.getMarketName())
                .orElseThrow(() -> new RuntimeException("최적화 결과가 존재하지 않습니다."));

        // DTO에 데이터 매핑
        BestOptimizedResultDTO dto = new BestOptimizedResultDTO();
        dto.marketName = result.getMarketName();
        dto.memoId = memoId;
        dto.totalPrice = result.getTotalPrice();
        dto.distance = result.getDistance();
        dto.items = new ArrayList<>();

        for (OptimizedResultItem item : result.getItems()) {
            BestOptimizedResultDTO.ItemDTO itemDTO = new BestOptimizedResultDTO.ItemDTO();
            itemDTO.memoItemName = item.getMemoItemName();
            itemDTO.productName = item.getProductName();
            itemDTO.price = item.getPrice();
            dto.items.add(itemDTO);
        }

        return dto;
    }

    @Transactional
    public void optimizeOfflineMarketCombinations(Long memoId) {
        Memo memo = memoRepo.findByIdWithItems(memoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid memo ID"));

        // 온라인으로 확정되지 않은 품목만 대상으로
        List<MemoItem> remainingItems = memo.getMemoItems().stream()
                .filter(item -> !item.isPurchasedOnline())
                .collect(Collectors.toList());

        // 오프라인 마트만 필터링
        List<Market> offlineMarkets = marketRepo.findAll().stream()
                .filter(market -> !market.isOnline())
                .collect(Collectors.toList());

        // 모든 조합 생성 (1개 ~ 4개 선택)
        List<List<Market>> marketCombinations = generateCombinations(offlineMarkets, 1, 4);

        for (List<Market> marketCombo : marketCombinations) {
            int totalPrice = 0;

            for (MemoItem item : remainingItems) {
                // 추천된 상품 중에서 현재 조합의 마트에 속한 상품만 필터링
                List<MemoItemProduct> candidates = memoItemProductRepository.findByMemoItemAndRecommendedTrue(item).stream()
                        .filter(p -> marketCombo.contains(p.getProducts().getMarket()))
                        .collect(Collectors.toList());

                Optional<MemoItemProduct> cheapest = candidates.stream()
                        .min(Comparator.comparingInt(p -> p.getProducts().getPrice()));

                if (cheapest.isPresent()) {
                    totalPrice += cheapest.get().getProducts().getPrice();
                } else {
                    // 해당 품목을 이 마트 조합에서는 살 수 없음 → 이 조합은 무효
                    totalPrice = Integer.MAX_VALUE;
                    break;
                }
            }

            if (totalPrice < Integer.MAX_VALUE) {
                // 유효한 조합일 경우 DTO나 Entity로 저장 (예시)
                OfflineCombinationResult result = new OfflineCombinationResult();
                result.setMemo(memo);
                result.setMarkets(marketCombo);
                result.setTotalPrice(totalPrice);

                calculateAndSaveTspDistance(result); // ✅ 추가된 거리 계산

                offlineCombinationResultRepository.save(result); // ✅ 계산 후 저장
            }
        }
    }

    // 조합 생성 유틸 메서드
    private void calculateAndSaveTspDistance(OfflineCombinationResult result) {
        List<Market> markets = result.getMarkets();
        double[][] distanceMatrix = distanceMatrixService.buildMatrix(markets); // 또는 직접 생성

        long tspDistance = TSPSolver.solveTsp(distanceMatrix);
        result.setTotalDistance((double) tspDistance);
    }

    private <T> List<List<T>> generateCombinations(List<T> items, int min, int max) {
        List<List<T>> result = new ArrayList<>();
        int n = items.size();
        for (int r = min; r <= max; r++) {
            combineRecursive(items, result, new ArrayList<>(), 0, n, r);
        }
        return result;
    }

    private <T> void combineRecursive(List<T> items, List<List<T>> result,
                                      List<T> temp, int start, int n, int r) {
        if (r == 0) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < n; i++) {
            temp.add(items.get(i));
            combineRecursive(items, result, temp, i + 1, n, r - 1);
            temp.remove(temp.size() - 1);
        }
    }



    // 최적화 결과를 분석쪽으로
    @Transactional
    public void generateOptimizedMarketCartsAndBindToAnalysis(Memo memo, Analysis analysis) {

        generateOptimizedMarketCarts(memo.getId(), memo.getUserLat(), memo.getUserLng());

        List<OptimizedResult> optimizedResults = optimizedResultRepository.findByMemo(memo);

        for (OptimizedResult optimizedResult : optimizedResults) {
            List<OptimizedResultItem> optimizedItems = optimizedResult.getItems();

            if (optimizedItems.isEmpty()) {
                System.out.println("OptimizedResult(" + optimizedResult.getMarketName() + ")에 OptimizedResultItem이 비어있습니다. generateOptimizedMarketCarts를 다시 확인하세요.");
                continue; // 이 마트에 대한 상품이 없으므로 다음 마트로
            }

            Market mart = marketRepo.findByName(optimizedResult.getMarketName())
                    .orElseThrow(() -> new RuntimeException("마트를 찾을 수 없습니다: " + optimizedResult.getMarketName()));

            for (OptimizedResultItem item : optimizedItems) {
                RecommendationResult rr = new RecommendationResult();
                rr.setAnalysis(analysis);
                rr.setMart(mart);

                String target = item.getProductName().replaceAll("\\s+", "").toLowerCase();

                List<Product> candidates = productRepository.findAll().stream()
                        .filter(p -> {
                            String productName = p.getName().replaceAll("\\s+", "").toLowerCase();
                            return productName.contains(target) || target.contains(productName);
                        })
                        .toList();


                if (candidates.isEmpty()) {
                    System.out.println("❌ 상품을 찾을 수 없습니다: " + item.getProductName());
                    continue;
                }

                Product product = candidates.get(0);

                rr.setProduct(product);

                rr.setTotalPrice((double) item.getPrice()); // 상품 개별 가격
                rr.setDistance(optimizedResult.getDistance());
                rr.setDeliveryFee(0.0);
                rr.setScore(0.0);

                recommendationResultRepository.save(rr);
            }
        }
    }

    @Transactional
    public void splitOnlineOfflineSelections(Long memoId) {
        Memo memo = memoRepo.findByIdWithItems(memoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid memo ID"));

        List<MemoItem> items = memo.getMemoItems();
        OptimizedResult optimizedResult = optimizedResultRepository.findByMemoId(memoId)
                .orElseThrow(() -> new IllegalStateException("No OptimizedResult found"));

        for (MemoItem item : items) {
            // 추천된 상품 중에서 필터링
            List<MemoItemProduct> recommended = memoItemProductRepository.findByMemoItemAndRecommendedTrue(item);

            // 온라인 최저가
            Optional<MemoItemProduct> onlineBest = recommended.stream()
                    .filter(p -> p.getProducts().getMarket().isOnline())
                    .min(Comparator.comparingInt(p -> p.getProducts().getPrice()));

            // 오프라인 최저가
            Optional<MemoItemProduct> offlineBest = recommended.stream()
                    .filter(p -> !p.getProducts().getMarket().isOnline())
                    .min(Comparator.comparingInt(p -> p.getProducts().getPrice()));

            // 둘 다 있을 경우 비교
            if (onlineBest.isPresent() && offlineBest.isPresent()) {
                int onlinePrice = onlineBest.get().getProducts().getPrice();
                int offlinePrice = offlineBest.get().getProducts().getPrice();

                if (onlinePrice < offlinePrice) {
                    // 온라인이 더 쌈 → 온라인 구매 확정
                    Products product = onlineBest.get().getProducts();

                    OptimizedResultItem resultItem = new OptimizedResultItem();
                    resultItem.setMemoItem(item);
                    resultItem.setOptimizedResult(optimizedResult);
                    resultItem.setMarket(product.getMarket());
                    resultItem.setProducts(product);
                    resultItem.setPrice(product.getPrice());
                    resultItem.setOnline(true); // ← 구분 플래그

                    optimizedResultItemRepository.save(resultItem);

                    // 추후 offline 최적화에서 제외될 수 있게 별도 리스트나 상태 저장 가능
                    item.setPurchasedOnline(true); // 예시: MemoItem에 boolean 필드 추가
                }
            }
        }
    }

    public void calculateOfflineCombinationScores(Long memoId, double priceWeight, double distanceWeight) {
        List<OfflineCombinationResult> results = offlineCombinationResultRepository.findByMemoId(memoId);
        int minPrice = results.stream().mapToInt(OfflineCombinationResult::getTotalPrice).min().orElse(0);
        int maxPrice = results.stream().mapToInt(OfflineCombinationResult::getTotalPrice).max().orElse(1);
        double minDistance = results.stream().mapToDouble(OfflineCombinationResult::getTotalDistance).min().orElse(0);
        double maxDistance = results.stream().mapToDouble(OfflineCombinationResult::getTotalDistance).max().orElse(1);

        for (OfflineCombinationResult result : results) {
            double normalizedPrice = (result.getTotalPrice() - minPrice) / (double)(maxPrice - minPrice);
            double normalizedDistance = (result.getTotalDistance() - minDistance) / (maxDistance - minDistance);

            double score = (normalizedPrice * priceWeight) + (normalizedDistance * distanceWeight);
            result.setScore(score);
            offlineCombinationResultRepository.save(result);
        }
    }



    @Transactional
    public BestOptimizedResultDTO optimizeAll(Long memoId, String option) {
        Memo memo = memoRepo.findByIdWithItems(memoId).orElseThrow();

        // Step 1~2: GPT 추천 + 최저가 + 온라인 분리
        generateOptimizedMarketCarts(memoId, memo.getUserLat(), memo.getUserLng());
        splitOnlineOfflineSelections(memoId);

        // Step 3~4: 오프라인 조합 최적화 + 거리 계산
        optimizeOfflineMarketCombinations(memoId);

        // Step 5: 점수 계산
        switch (option.toUpperCase()) {
            case "PRICE" -> calculateOfflineCombinationScores(memoId, 0.8, 0.2);
            case "DISTANCE" -> calculateOfflineCombinationScores(memoId, 0.2, 0.8);
            default -> calculateOfflineCombinationScores(memoId, 0.5, 0.5); // BALANCED
        }

        // Step 6: 최적 결과 반환
        return getBestOptimizedResultForMemo(memoId);
    }

}
