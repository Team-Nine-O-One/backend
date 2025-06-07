package com.team901.CapstoneDesign;

import com.team901.CapstoneDesign.dto.BestOptimizedResultDTO;
import com.team901.CapstoneDesign.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scores")
public class ScoreController {

    private final MemoService scoreService;

    // 점수 계산 수행
    @PostMapping("/calculate")
    public ResponseEntity<String> calculateScore(
            @RequestParam Long memoId,
            @RequestParam double priceWeight,
            @RequestParam double distanceWeight) {

        scoreService.calculateScoresForMemo(memoId, priceWeight, distanceWeight);
        return ResponseEntity.ok("점수 계산 완료");
    }

    // 최고 점수 결과 조회
    @GetMapping("/best")
    public ResponseEntity<BestOptimizedResultDTO> getBestResult(@RequestParam Long memoId) {
        BestOptimizedResultDTO dto = scoreService.getBestOptimizedResultForMemo(memoId);
        return ResponseEntity.ok(dto);
    }
}