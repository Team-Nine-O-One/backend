package com.team901.CapstoneDesign.carts.controller;

import com.team901.CapstoneDesign.carts.dto.*;
import com.team901.CapstoneDesign.carts.service.AnalysisService;

import com.team901.CapstoneDesign.dto.BestOptimizedResultDTO;
import com.team901.CapstoneDesign.global.enums.CartStatus;
import com.team901.CapstoneDesign.mart.repository.MartRepository;
import com.team901.CapstoneDesign.product.repository.ProductRepository;
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

    @Operation(summary = "분석 요청 생성", description = "메모 내용을 기반으로 분석 생성")
    @PostMapping
    public ResponseEntity<AnalysisResponseDto> createAnalysis(@RequestBody AnalysisRequestDto requestDto) {
        AnalysisResponseDto responseDto = analysisService.createAnalysis(requestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @Operation(summary = "최적화 전체 실행", description = "GPT 추천부터 최종 선택까지 자동 실행")
    @PostMapping("/{memoId}/optimize")
    public ResponseEntity<BestOptimizedResultDTO> optimizeAll(
            @PathVariable Long memoId,
            @RequestParam(defaultValue = "BALANCED") String option
    ) {
        BestOptimizedResultDTO response = analysisService.optimizeMemo(memoId, option);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cartId}/reanalyze")
    @Operation(summary = "분석 재실행", description = "가중치 변경에 따른 재분석 실행")
    public ResponseEntity<AnalysisResponseDto> reanalyzeCart(
            @PathVariable Long cartId,
            @RequestBody ReanalyzeRequestDto requestDto
    ) {
        AnalysisResponseDto response = analysisService.reanalyze(cartId, requestDto);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(summary = "분석 목록 조회", description = "사용자의 전체 또는 상태별 분석 조회")
    public ResponseEntity<List<CartSummaryResponseDto>> getCarts(
            @RequestParam("user_id") String userId,
            @RequestParam(value = "status", required = false) CartStatus status
    ) {
        List<CartSummaryResponseDto> responseDtos = analysisService.getCartsByUserAndStatus(userId, status);
        return ResponseEntity.ok(responseDtos);
    }


    @Operation(summary = "특정 분석 상세 조회", description = "cartId에 해당하는 상세 분석 정보를 조회")
    @GetMapping("/{cartId}")


    public ResponseEntity<GroupedCartDetailResponseDto> getCartDetails(@PathVariable Long cartId) {
        GroupedCartDetailResponseDto responseDto = analysisService.getGroupedCartDetails(cartId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "분석 결과 확정", description = "사용자가 분석 결과를 최종적으로 확정")
    @PostMapping("/{cartId}/confirm")
    public ResponseEntity<ConfirmCartResponseDto> confirmCart(
            @PathVariable Long cartId) {
        ConfirmCartResponseDto responseDto = analysisService.confirmCart(cartId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "장보기 완료", description = "사용자가 장을 본 후 완료 처리 및 히스토리에 저장")
    @PostMapping("/{cartId}/complete")
    public ResponseEntity<CompleteCartResponseDto> completeCart(
            @PathVariable Long cartId) {
        CompleteCartResponseDto responseDto = analysisService.completeCart(cartId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "히스토리 삭제", description = "CONFIRMED 또는 COMPLETED 상태의 장바구니 분석을 삭제")
    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable Long cartId) {
        analysisService.deleteCart(cartId);
        return ResponseEntity.ok("장바구니가 삭제되었습니다.");
    }


}

