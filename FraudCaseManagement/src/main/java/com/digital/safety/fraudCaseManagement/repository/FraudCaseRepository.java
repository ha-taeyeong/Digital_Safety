package com.digital.safety.fraudCaseManagement.repository;

import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FraudCaseRepository extends JpaRepository<FraudCaseEntity, Long> {

    // 전체 목록 조회 (MainController 등에서 사용)
    List<FraudCaseEntity> findAllByTypeIdAndDelYnFalse(Integer typeId);

    // 페이징 적용된 목록 조회
    // 반환 타입이 List가 아니라 'Page' 입니다.
    Page<FraudCaseEntity> findAllByTypeIdAndDelYnFalse(Integer typeId, Pageable pageable);

    @Query("SELECT f.typeId, COUNT(f) FROM FraudCaseEntity f WHERE f.delYn = false GROUP BY f.typeId")
    List<Object[]> countCasesByTypeId();
}