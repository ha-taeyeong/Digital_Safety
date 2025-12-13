package com.digital.safety.fraudCaseManagement.dto;

import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import java.time.LocalDateTime;

public class FraudCaseResponse {

    private Long id;            // HTML에서 item.id로 사용
    private String orgName;     // HTML에서 item.orgName (기관명)
    private String title;       // HTML에서 item.title
    private String description; // HTML에서 item.description (내용)
    private LocalDateTime createdDate; // HTML에서 item.createdDate
    private String orgPhone;    // 기관 공식 전화번호도 화면에 보여주려면 추가
    // [추가] 조회수 필드
    private int viewCount; 
    
    // [추가] 좋아요 수 필드
    private int likeCount;

    // --- 기본 생성자 ---
    public FraudCaseResponse() {
    }

    // --- Entity -> DTO 변환 생성자 ---
    public FraudCaseResponse(FraudCaseEntity entity) {
        this.id = entity.getCaseId();
        this.title = entity.getTitle();
        this.description = entity.getContent(); // Entity의 content를 DTO의 description으로 매핑
        this.createdDate = entity.getRegiDt();  // Entity의 regiDt를 DTO의 createdDate로 매핑
        this.viewCount = entity.getViewCount();
        this.likeCount = entity.getLikeCount();

        // [핵심] 기관 정보(Join) 데이터 꺼내기
        // entity.getAgency()를 통해 부모 테이블(PUBLIC_AGENCY) 정보에 접근합니다.
        if (entity.getAgency() != null) {
            this.orgName = entity.getAgency().getAgencyName(); // "고용노동부"
            this.orgPhone = entity.getAgency().getPhoneNo();   // "1350"
        } else {
            this.orgName = "기타/알수없음";
            this.orgPhone = "";
        }
    }
    
    // --- Getter & Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getOrgPhone() { return orgPhone; }
    public void setOrgPhone(String orgPhone) { this.orgPhone = orgPhone; }
    
    // [추가] Getter & Setter
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}