package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.Memo;
import com.team901.CapstoneDesign.entity.OptimizedResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OptimizedResultRepository extends JpaRepository<OptimizedResult, Long> {
    List<OptimizedResult> findByMemoId(Long memoId);
    List<OptimizedResult> findByMemo(Memo memo);
    Optional<OptimizedResult> findByMemoIdAndMarketName(Long memoId, String marketName);
}