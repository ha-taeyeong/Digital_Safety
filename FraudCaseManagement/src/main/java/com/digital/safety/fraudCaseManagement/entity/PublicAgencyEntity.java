package com.digital.safety.fraudCaseManagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PUBLIC_AGENCY")
public class PublicAgencyEntity {

	@Id
    @Column(name = "AGENCY_ID", length = 20)
    private String id; // 예: 'moel', 'nts'

    @Column(name = "AGENCY_NAME", length = 50, nullable = false)
    private String agencyName; // 예: '고용노동부'

    @Column(name = "HOMEPAGE_URL", length = 200)
    private String homepageUrl;

    @Column(name = "PHONE_NO", length = 20)
    private String phoneNo;

    // --- 기본 생성자 ---
    public PublicAgencyEntity() {}
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}

	public String getHomepageUrl() {
		return homepageUrl;
	}

	public void setHomepageUrl(String homepageUrl) {
		this.homepageUrl = homepageUrl;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
    
    
}