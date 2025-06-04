package com.team901.CapstoneDesign.service;

import com.team901.CapstoneDesign.GPT.service.GPTService;
import com.team901.CapstoneDesign.dto.BestOptimizedResultDTO;
import com.team901.CapstoneDesign.dto.MarketCartResponseDTO;
import com.team901.CapstoneDesign.dto.MemoRequestDTO;
import com.team901.CapstoneDesign.entity.*;
import com.team901.CapstoneDesign.product.repository.ProductRepository;
import com.team901.CapstoneDesign.repository.*;
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



    public Memo createMemoWithItems(MemoRequestDTO dto) {
        Memo memo = new Memo();
        memo.setRawText(dto.rawText);
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

            if (market.getType() == MarketType.OFFLINE) {
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
        prompt.append("위의 상품 중에서 가장 유사한 3가지를 골라주세요. 이름만 반환하세요.");

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

            if (market.getType() == MarketType.OFFLINE) {
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









}
