package com.team901.CapstoneDesign.carts.repository;

import com.team901.CapstoneDesign.carts.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team901.CapstoneDesign.global.enums.CartStatus;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserId(String userId);

    List<Cart> findByUserIdAndStatus(String userId, CartStatus status);

}
