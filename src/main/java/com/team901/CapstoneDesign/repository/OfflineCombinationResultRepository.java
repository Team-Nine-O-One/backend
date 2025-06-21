package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.OfflineCombinationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OfflineCombinationResultRepository extends JpaRepository<OfflineCombinationResult, Long> {
    List<OfflineCombinationResult> findByMemoId(Long memoId);
}