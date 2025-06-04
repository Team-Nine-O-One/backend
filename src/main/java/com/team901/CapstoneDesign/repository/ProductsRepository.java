package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductsRepository extends JpaRepository<Products, Long> {
    List<Products> findByNameContainingIgnoreCase(String name);
}
