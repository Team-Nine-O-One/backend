package com.team901.CapstoneDesign.carts.controller;

import com.team901.CapstoneDesign.carts.dto.*;
import com.team901.CapstoneDesign.carts.service.AnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "분석 요청 생성 (아직 사용 x)", description = "메모 내용을 기반으로 분석 생성")
    @PostMapping
    public ResponseEntity<AnalysisResponseDto> createAnalysis(@RequestBody AnalysisRequestDto requestDto) {
        AnalysisResponseDto responseDto = analysisService.createAnalysis(requestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @Operation(summary = "모든 분석 목록 조회", description = "모든 장바구니 분석 목록을 조회")
    @GetMapping
    public ResponseEntity<List<CartSummaryResponseDto>> getAllCarts(@RequestParam("user_id") String userId) {
        List<CartSummaryResponseDto> responseDtos = analysisService.getAllCartsByUser(userId);
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "특정 분석 상세 조회", description = "cartId에 해당하는 상세 분석 정보를 조회")
    @GetMapping("/{cartId}")
    public ResponseEntity<CartDetailResponseDto> getCartDetails(@PathVariable Long cartId) {
        CartDetailResponseDto responseDto = analysisService.getCartDetails(cartId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "임의 분석 생성 (테스트용)", description = "임시 데이터를 통해 테스트용 분석을 생성")
    @PostMapping("/test")
    public ResponseEntity<String> createCartForTest(@RequestBody CartTestRequestDto requestDto) {
        analysisService.createCartForTest(requestDto);
        return ResponseEntity.ok("테스트용 Cart 생성 완료");

    }


}

