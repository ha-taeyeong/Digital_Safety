package com.digital.safety.fraudCaseManagement.repository;

import com.digital.safety.fraudCaseManagement.entity.PublicAgencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicAgencyRepository extends JpaRepository<PublicAgencyEntity, String> {
    // 기본 CRUD(findById 등)는 JpaRepository가 자동으로 만들어주므로 비워둬도 됩니다.
    // ID 타입이 String('moel')이므로 <Entity, String>으로 지정합니다.
}