package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarketRepository extends JpaRepository<Market, Long> {

    Optional<Market> findByName(String name);
}