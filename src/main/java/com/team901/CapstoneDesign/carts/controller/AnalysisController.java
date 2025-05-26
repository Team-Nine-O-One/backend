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

    @Operation(summary = "분석 결과 확정", description = "사용자가 분석 결과를 최종적으로 확정")
    @PostMapping("/{cartId}/confirm")
    public ResponseEntity<ConfirmCartResponseDto> confirmCart(
            @PathVariable Long cartId,
            @RequestParam("user_id") String userId) {
        ConfirmCartResponseDto responseDto = analysisService.confirmCart(cartId, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "장보기 완료", description = "사용자가 장을 본 후 완료 처리 및 히스토리에 저장")
    @PostMapping("/{cartId}/complete")
    public ResponseEntity<CompleteCartResponseDto> completeCart(
            @PathVariable Long cartId,
            @RequestParam("user_id") String userId) {
        CompleteCartResponseDto responseDto = analysisService.completeCart(cartId, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "히스토리 삭제", description = "CONFIRMED 또는 COMPLETED 상태의 장바구니 분석을 삭제")
    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable Long cartId,
                                             @RequestParam("user_id") String userId) {
        analysisService.deleteCart(cartId, userId);
        return ResponseEntity.ok("장바구니가 삭제되었습니다.");
    }


}

