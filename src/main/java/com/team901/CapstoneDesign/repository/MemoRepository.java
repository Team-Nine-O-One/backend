package com.team901.CapstoneDesign.repository;

import com.team901.CapstoneDesign.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    @Query("SELECT m FROM Memo m LEFT JOIN FETCH m.memoItems WHERE m.id = :id")
    Optional<Memo> findByIdWithItems(@Param("id") Long id);
}
