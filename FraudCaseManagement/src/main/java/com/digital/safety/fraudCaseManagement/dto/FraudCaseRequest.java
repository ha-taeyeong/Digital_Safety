package com.digital.safety.fraudCaseManagement.dto;

// DTO는 DB 필드 중 클라이언트가 입력할 데이터만 가집니다.
public class FraudCaseRequest {

    // 1. 사기 유형 구분 (1. 구인사기, 2.정부,공공기관 사칭)
    private Integer typeId;

    // 2. 제목
    private String title;

    // 3. 상세 내용
    private String content;
    
    // 4. 공공기관 구분
    private String agencyId;

    // 5. [추가] 게시글 수정/삭제용 비밀번호
    private String guestPw;
    

    // --- 기본 생성자 ---
    public FraudCaseRequest() {
    }
    
    // --- Getter & Setter ---
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

    // [추가된 비밀번호 Getter/Setter]
    public String getGuestPw() {
        return guestPw;
    }

    public void setGuestPw(String guestPw) {
        this.guestPw = guestPw;
    }
}