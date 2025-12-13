package com.digital.safety.fraudCaseManagement.service;

import com.digital.safety.fraudCaseManagement.dto.FraudCaseRequest;
import com.digital.safety.fraudCaseManagement.dto.FraudCaseResponse;
import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import com.digital.safety.fraudCaseManagement.entity.PublicAgencyEntity;
import com.digital.safety.fraudCaseManagement.repository.FraudCaseRepository;
import com.digital.safety.fraudCaseManagement.repository.PublicAgencyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FraudCaseService {

    private final FraudCaseRepository repository;
    private final PublicAgencyRepository agencyRepository;

    public FraudCaseService(FraudCaseRepository repository, PublicAgencyRepository agencyRepository) {
        this.repository = repository;
        this.agencyRepository = agencyRepository;
    }

    // 1. [저장] 비밀번호 포함 저장
    @Transactional
    public Long createCase(FraudCaseRequest request) {
        FraudCaseEntity entity = new FraudCaseEntity();
        entity.setTypeId(request.getTypeId() != null ? request.getTypeId() : 1);
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setDelYn(false);
        entity.setRegiDt(LocalDateTime.now());
        
        // ★ 비밀번호 저장
        entity.setGuestPw(request.getGuestPw());

        if (request.getAgencyId() != null) {
            PublicAgencyEntity agency = agencyRepository.findById(request.getAgencyId())
                    .orElse(agencyRepository.findById("etc").orElse(null));
            entity.setAgency(agency);
        }
        return repository.save(entity).getCaseId();
    }

    // 2. [검증] 비밀번호 확인용 공통 메서드
    @Transactional(readOnly = true)
    public boolean verifyPassword(Long caseId, String inputPw) {
        FraudCaseEntity entity = repository.findById(caseId).orElse(null);
        if (entity == null || entity.getGuestPw() == null) return false;
        
        // DB 비밀번호와 입력값 비교
        return entity.getGuestPw().equals(inputPw);
    }

    // 3. [수정] 비밀번호 검증 후 업데이트
    @Transactional
    public boolean updateCase(Long caseId, FraudCaseRequest request) {
        // (1) 비밀번호 검증 (DTO에 있는 비번 사용)
        if (!verifyPassword(caseId, request.getGuestPw())) {
            return false; // 실패
        }

        // (2) 내용 수정
        FraudCaseEntity entity = repository.findById(caseId).orElseThrow();
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setModiDt(LocalDateTime.now());

        if (request.getAgencyId() != null) {
            PublicAgencyEntity agency = agencyRepository.findById(request.getAgencyId())
                    .orElse(agencyRepository.findById("etc").orElse(null));
            entity.setAgency(agency);
        }
        return true; // 성공
    }

    // 4. [삭제] 비밀번호 검증 후 삭제 (Soft Delete)
    @Transactional
    public boolean deleteCase(Long caseId, String inputPw) {
        // (1) 비밀번호 검증
        if (!verifyPassword(caseId, inputPw)) {
            return false; // 실패
        }

        // (2) 삭제 처리
        FraudCaseEntity entity = repository.findById(caseId).orElseThrow();
        entity.setDelYn(true);    
        return true; // 성공
    }
    
    // --- [수정 및 추가된 로직] 좋아요/조회수 처리 ---

    /**
     * [수정됨] 조회수를 1 증가시킵니다.
     * @param caseId 조회수를 증가시킬 게시글 ID
     */
    @Transactional
    public void increaseViewCount(Long caseId) {
        FraudCaseEntity entity = repository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("조회수 증가 실패: 해당 게시글 ID 없음"));
        
        // Entity의 비즈니스 로직 호출
        entity.increaseViewCount();
        repository.save(entity); // DB에 변경사항 반영
    }

    /**
     * [추가됨] 좋아요를 증감시키고 카운트를 반환합니다.
     * @param caseId 게시글 ID
     * @param change 증감 값 (+1 또는 -1)
     * @return 변경된 좋아요 카운트
     */
    @Transactional
    public int toggleLike(Long caseId, int change) {
        FraudCaseEntity entity = repository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 토글 실패: 해당 게시글 ID 없음"));
        
        // Entity의 비즈니스 로직 호출
        entity.updateLikeCount(change);
        repository.save(entity); // DB에 변경사항 반영
        
        return entity.getLikeCount();
    }
    
    // --- (기타 조회 및 통계 메서드는 그대로 유지) ---
    
    @Transactional(readOnly = true)
    public List<FraudCaseResponse> findAllByTypeId(Integer typeId) {
        return repository.findAllByTypeIdAndDelYnFalse(typeId).stream()
                .map(FraudCaseResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FraudCaseEntity findCaseById(Long caseId) {
        // 조회수 증가 로직은 이 메서드를 호출하는 컨트롤러에서 별도로 처리해야 함.
        return repository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID 없음: " + caseId));
    }

    @Transactional(readOnly = true)
    public Page<FraudCaseResponse> findAllByTypeId(Integer typeId, Pageable pageable) {
        return repository.findAllByTypeIdAndDelYnFalse(typeId, pageable).map(FraudCaseResponse::new);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFraudStatistics() {
        List<Object[]> stats = repository.countCasesByTypeId();
        long totalCount = stats.stream().mapToLong(s -> (Long) s[1]).sum();
        String[] labels = {"기타", "구인 사기", "정부·공공기관", "텔레그램", "금융기관"};
        Double[] data = {0.0, 0.0, 0.0, 0.0, 0.0};
        for (Object[] row : stats) {
            int typeId = (Integer) row[0];
            long count = (Long) row[1];
            if (typeId >= 1 && typeId <= 4) {
                data[typeId] = Math.round((count / (double) totalCount) * 1000) / 10.0;
            }
        }
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", Arrays.asList(labels[1], labels[2], labels[3], labels[4]));
        chartData.put("data", Arrays.asList(data[1], data[2], data[3], data[4]));
        return chartData;
    }
    
    // 이전에 잘못 복사되어 들어간 코드들을 모두 제거합니다.
    // public void increaseViewCount() { this.viewCount++; }
    // public void updateLikeCount(int change) { ... }
}