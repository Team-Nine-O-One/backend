package com.team901.CapstoneDesign.carts.repository;

import com.team901.CapstoneDesign.carts.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
}
