package com.team901.CapstoneDesign;

import com.team901.CapstoneDesign.dto.MarketCartResponseDTO;
import com.team901.CapstoneDesign.dto.MemoRequestDTO;
import com.team901.CapstoneDesign.entity.Market;
import com.team901.CapstoneDesign.entity.MarketType;
import com.team901.CapstoneDesign.entity.Memo;
import com.team901.CapstoneDesign.entity.Products;
import com.team901.CapstoneDesign.repository.MarketRepository;
import com.team901.CapstoneDesign.repository.MemoRepository;
import com.team901.CapstoneDesign.repository.ProductsRepository;
import com.team901.CapstoneDesign.service.MemoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class MemoServiceTest {

    @Autowired
    private MemoService memoService;

    @Autowired
    private MemoRepository memoRepository;

    @Autowired
    private ProductsRepository productRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Test
    void testCreateMemoWithItems() {
        // given
        MemoRequestDTO dto = new MemoRequestDTO();
        dto.rawText = "당근 한개 | 우유 한개";
        dto.userLat = 37.5665;
        dto.userLng = 126.9780;

        // when
        Memo memo = memoService.createMemoWithItems(dto);

        // then
        Assertions.assertNotNull(memo.getId());
        Assertions.assertEquals(2, memo.getMemoItems().size());
    }

    @Test
    void testGenerateMarketCarts() {
        // given
        Market market1 = new Market();
        market1.setName("쿠팡");
        market1.setType(MarketType.ONLINE);
        marketRepository.save(market1);

        Products product = new Products();
        product.setName("당근 1kg");
        product.setPrice(1200);
        product.setMarket(market1);
        productRepository.save(product);

        MemoRequestDTO dto = new MemoRequestDTO();
        dto.rawText = "당근 한개";
        dto.userLat = 37.5665;
        dto.userLng = 126.9780;
        Memo memo = memoService.createMemoWithItems(dto);

        // when
        List<MarketCartResponseDTO> result = memoService.generateMarketCarts(memo.getId(), dto.userLat, dto.userLng);

        // then
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("쿠팡", result.get(0).marketName);
        Assertions.assertEquals(1200, result.get(0).totalPrice);
    }
}