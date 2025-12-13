package com.digital.safety.fraudCaseManagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // 이 클래스가 DB 테이블에 매핑됨을 명시
@Table(name = "FRAUD_CASE_CONTENT") // 실제 테이블 이름 지정
public class FraudCaseEntity {

    @Id // PK 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment (BIGINT) 설정
    private Long caseId; // DB: BIGINT -> Java: Long

    @Column(name = "TYPE_ID")
    private Integer typeId; // DB: INT -> Java: Integer

    @Column(name = "TITLE", length = 200)
    private String title; // DB: VARCHAR(200)

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    private String content; // DB: TEXT

    @Column(name = "REGI_DT")
    private LocalDateTime regiDt; // DB: DATETIME

    @Column(name = "MODI_DT")
    private LocalDateTime modiDt; // DB: DATETIME

    @Column(name = "DEL_YN")
    private Boolean delYn = false; // DB: BOOLEAN

    @Column(name = "GUEST_PW", length = 20)
    private String guestPw;
    
    // --- [추가 1] 조회수 및 좋아요 수 필드 (기본값 0 설정) ---
    @Column(name = "VIEW_COUNT", nullable = false)
    private int viewCount = 0;

    @Column(name = "LIKE_COUNT", nullable = false)
    private int likeCount = 0;
    
    // 기관 정보와 조인 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGENCY_ID")
    private PublicAgencyEntity agency;

    // --- 기본 생성자 ---
    public FraudCaseEntity() {
    }
    
    /**
     * 조회수를 1 증가시킵니다.
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 좋아요 카운트를 주어진 값만큼 증감시킵니다.
     * @param change 증감 값 (+1 또는 -1)
     */
    public void updateLikeCount(int change) {
        this.likeCount += change;
        // 좋아요 수가 0 미만으로 내려가지 않도록 안전장치 추가
        if (this.likeCount < 0) {
            this.likeCount = 0;
        }
    }
    
    // --- Getter & Setter (Lombok 미사용) ---

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getRegiDt() { return regiDt; }
    public void setRegiDt(LocalDateTime regiDt) { this.regiDt = regiDt; }

    public LocalDateTime getModiDt() { return modiDt; }
    public void setModiDt(LocalDateTime modiDt) { this.modiDt = modiDt; }

    public Boolean getDelYn() { return delYn; }
    public void setDelYn(Boolean delYn) { this.delYn = delYn; }

    public String getGuestPw() { return guestPw; }
    public void setGuestPw(String guestPw) { this.guestPw = guestPw; }

    public PublicAgencyEntity getAgency() { return agency; }
    public void setAgency(PublicAgencyEntity agency) { this.agency = agency; }
    
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    
}