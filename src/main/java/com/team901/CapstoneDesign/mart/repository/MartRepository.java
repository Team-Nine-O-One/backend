package com.team901.CapstoneDesign.mart.repository;

import com.team901.CapstoneDesign.mart.entity.Mart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MartRepository extends JpaRepository<Mart, Long> {
    Optional<Mart> findByName(String name);
}
