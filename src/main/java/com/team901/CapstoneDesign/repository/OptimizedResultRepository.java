package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.Memo;
import com.team901.CapstoneDesign.entity.OptimizedResult;
import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OptimizedResultRepository extends JpaRepository<OptimizedResult, Long> {
    Optional<OptimizedResult> findByMemoId(Long memoId);

    @Query("SELECT r FROM OptimizedResult r LEFT JOIN FETCH r.items WHERE r.memo = :memo")
    List<OptimizedResult> findByMemo(@Param("memo") Memo memo);



    Optional<OptimizedResult> findByMemoIdAndMarketName(Long memoId, String marketName);
}