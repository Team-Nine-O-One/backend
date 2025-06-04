package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findTopByMemoIdOrderByScoreDesc(Long memoId);


}
