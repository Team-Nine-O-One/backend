package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.MemoItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoItemRepository extends JpaRepository<MemoItem, Long> {}