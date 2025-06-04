package com.team901.CapstoneDesign.carts.repository;

import com.team901.CapstoneDesign.carts.entity.Analysis;
import com.team901.CapstoneDesign.carts.entity.RecommendationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, Long> {
    List<RecommendationResult> findByAnalysis(Analysis analysis);
}

