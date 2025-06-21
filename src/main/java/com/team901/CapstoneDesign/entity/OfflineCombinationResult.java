package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineCombinationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결된 메모
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memo_id")
    private Memo memo;

    // 조합된 오프라인 마트 목록
    @ManyToMany
    @JoinTable(
            name = "offline_combination_result_market",
            joinColumns = @JoinColumn(name = "result_id"),
            inverseJoinColumns = @JoinColumn(name = "market_id")
    )
    private List<Market> markets = new ArrayList<>();

    // 해당 조합으로 살 경우 총 물건 가격
    private int totalPrice;

    // TSP 최적 경로 결과로 구한 거리 (단위: m 또는 km)
    private double totalDistance;

    // 정규화 점수 (0~1 사이)
    private double score;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}
