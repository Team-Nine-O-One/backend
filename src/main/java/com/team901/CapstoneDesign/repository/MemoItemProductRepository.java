package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.MemoItem;
import com.team901.CapstoneDesign.entity.MemoItemProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoItemProductRepository extends JpaRepository<MemoItemProduct, Long> {
    List<MemoItemProduct> findByMemoItemAndRecommendedTrue(MemoItem memoItem);
}