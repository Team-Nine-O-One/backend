package com.team901.CapstoneDesign.carts.controller;

import com.team901.CapstoneDesign.carts.dto.*;
import com.team901.CapstoneDesign.carts.service.AnalysisService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponseDto> createAnalysis(@RequestBody AnalysisRequestDto requestDto) {
        AnalysisResponseDto responseDto = analysisService.createAnalysis(requestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<CartSummaryResponseDto>> getAllCarts(@RequestParam("user_id") String userId) {
        List<CartSummaryResponseDto> responseDtos = analysisService.getAllCartsByUser(userId);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDetailResponseDto> getCartDetails(@PathVariable Long cartId) {
        CartDetailResponseDto responseDto = analysisService.getCartDetails(cartId);
        return ResponseEntity.ok(responseDto);
    }


    @PostMapping("/test")
    public ResponseEntity<String> createCartForTest(@RequestBody CartTestRequestDto requestDto) {
        System.out.println(">> DTO: " + requestDto.getUserId() + " / " + requestDto.getTitle());
        analysisService.createCartForTest(requestDto);
        return ResponseEntity.ok("테스트용 Cart 생성 완료");

    }


}

