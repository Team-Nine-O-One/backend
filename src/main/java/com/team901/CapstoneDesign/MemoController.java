package com.team901.CapstoneDesign;


import com.team901.CapstoneDesign.dto.MarketCartResponseDTO;
import com.team901.CapstoneDesign.dto.MemoRequestDTO;
import com.team901.CapstoneDesign.entity.Memo;
import com.team901.CapstoneDesign.service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memos")
public class MemoController {

    @Autowired
    private MemoService memoService;

    @PostMapping
    public ResponseEntity<String> createMemo(@RequestBody MemoRequestDTO dto) {
        Memo savedMemo = memoService.createMemoWithItems(dto);
        return ResponseEntity.ok("Memo saved with ID: " + savedMemo.getId());
    }

    @GetMapping("/{memoId}/carts")
    public ResponseEntity<List<MarketCartResponseDTO>> getMarketCarts(
            @PathVariable Long memoId,
            @RequestParam Double userLat,
            @RequestParam Double userLng) {
        List<MarketCartResponseDTO> result = memoService.generateMarketCarts(memoId, userLat, userLng);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/optimized")
    public List<MarketCartResponseDTO> createAndOptimizeCart(@RequestBody MemoRequestDTO dto) {
        Memo memo = memoService.createMemoWithItems(dto);
        return memoService.generateOptimizedMarketCarts(memo.getId(), dto.userLat, dto.userLng);
    }

    @GetMapping("/{memoId}/optimized")
    public List<MarketCartResponseDTO> getOptimizedCarts(
            @PathVariable Long memoId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        return memoService.generateOptimizedMarketCarts(memoId, lat, lng);



    }}