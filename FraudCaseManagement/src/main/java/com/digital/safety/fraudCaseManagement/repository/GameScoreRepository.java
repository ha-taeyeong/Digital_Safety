package com.digital.safety.fraudCaseManagement.repository;

import com.digital.safety.fraudCaseManagement.entity.GameScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameScoreRepository extends JpaRepository<GameScoreEntity, Long> {
    // 점수 높은 순으로 상위 5개 조회
    List<GameScoreEntity> findTop5ByOrderByScoreDesc();
}