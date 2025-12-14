package com.digital.safety.fraudCaseManagement.controller;

import com.digital.safety.fraudCaseManagement.dto.FraudCaseRequest;
import com.digital.safety.fraudCaseManagement.dto.FraudCaseResponse;
import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import com.digital.safety.fraudCaseManagement.service.FraudCaseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest; // 필수
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;

@Controller
public class FraudCaseViewController {

	private final FraudCaseService service;

	public FraudCaseViewController(FraudCaseService service) {
		this.service = service;
	}

	// --- [조회 기능] ---

	@GetMapping("/cases")
	public String viewJobFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
		Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(1, pageable);
		
        // 관리자 여부 확인하여 모델에 추가
		Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);
        
		model.addAttribute("cases", pagingResult);
		model.addAttribute("categoryName", "구인 사기");
		model.addAttribute("typeId", 1);
		return "scam_cases";
	}

    // ... (다른 목록 조회 메서드들도 위와 동일하게 isAdmin 추가 필요) ...
	@GetMapping("/cases/gov")
	public String viewGovFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
		Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(2, pageable);
        
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);

		model.addAttribute("cases", pagingResult);
		model.addAttribute("categoryName", "정부, 공공기관 사칭");
		model.addAttribute("typeId", 2);
		return "gov_list";
	}
	// 텔레그램 사기 목록 (Type ID: 3)
    @GetMapping("/cases/tele")
    public String viewTeleFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
        // 텔레그램 TypeId는 3번으로 가정 (HTML의 input hidden value="3"과 일치)
        Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(3, pageable);
        
        // 관리자 세션 확인 및 모델 추가
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);

        model.addAttribute("cases", pagingResult);
        model.addAttribute("categoryName", "텔레그램 사칭");
        model.addAttribute("typeId", 3);
        
        // 리턴하는 문자열은 resources/templates 폴더 안의 HTML 파일명과 일치해야 합니다.
        // 제공해주신 HTML 파일명이 'tele_list.html'이라면 "tele_list"로 설정하세요.
        return "tele_list"; 
    }

    // 금융 사기 목록 (Type ID: 4) - 필요시 함께 추가
    @GetMapping("/cases/finance")
    public String viewFinanceFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
        Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(4, pageable);
        
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);

        model.addAttribute("cases", pagingResult);
        model.addAttribute("categoryName", "금융/대출 사기");
        model.addAttribute("typeId", 4);
        
        return "finance_list"; // finance_list.html 파일이 있다고 가정
    }
	@GetMapping("/cases/{id}")
	public String viewDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
		service.increaseViewCount(id);
		FraudCaseEntity entity = service.findCaseById(id);
        
        // 상세 페이지에서도 관리자 권한이 필요할 수 있으므로 추가
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);
        
		model.addAttribute("case", entity);
		return "detail";
	}

	// --- [CUD 기능 : 관리자 권한 처리 포함] ---

	@GetMapping("/cases/new")
	public String viewAddForm(@RequestParam(required = false, defaultValue = "1") Integer type, Model model) {
		model.addAttribute("targetType", type);
		return "add-form";
	}

	@PostMapping("/cases/save")
	public String saveCase(FraudCaseRequest request) {
		service.createCase(request);
		int typeId = request.getTypeId();
        // 리다이렉트 로직 단순화
		if (typeId == 2) return "redirect:/cases/gov";
		else if (typeId == 3) return "redirect:/cases/tele";
		else if (typeId == 4) return "redirect:/cases/finance";
		else return "redirect:/cases";
	}

	// 3. [수정 폼 진입] 관리자면 비밀번호 검증 패스
	@GetMapping("/cases/{id}/edit")
	public String viewEditForm(@PathVariable Long id, @RequestParam("pw") String inputPw, Model model,
			HttpServletResponse response, HttpServletRequest request) throws Exception {

		// 세션에서 관리자 여부 확인
		Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        boolean isPass = false;

        if (isAdmin != null && isAdmin) {
            // 관리자라면 무조건 통과
            isPass = true;
        } else {
            // 관리자가 아니면 비밀번호 검증
            if (service.verifyPassword(id, inputPw)) {
                isPass = true;
            }
        }

        // 검증 실패 시 알림
		if (!isPass) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>alert('비밀번호가 일치하지 않습니다.'); history.back();</script>");
			out.flush();
			return null;
		}

		FraudCaseEntity entity = service.findCaseById(id);
		model.addAttribute("case", entity);
		model.addAttribute("guestPw", inputPw); // 수정 완료 처리를 위해 비밀번호 전달
		return "edit-form";
	}

	// ================================================================
    // [관리자 권한 처리 추가] 수정 완료 (POST)
    // ================================================================
	@PostMapping("/cases/{id}/edit")
    @ResponseBody
    public String updateCase(@PathVariable Long id, FraudCaseRequest request, HttpServletRequest httpRequest) {
        
        // 1. 세션에서 관리자 여부 확인
        Boolean isAdmin = (Boolean) httpRequest.getSession().getAttribute("isAdmin");
        
        // 2. 관리자라면? -> 폼에서 넘어온 비밀번호 무시하고, 진짜 비밀번호로 교체
        if (isAdmin != null && isAdmin) {
            // DB에서 현재 게시글 정보를 가져옴
            FraudCaseEntity realEntity = service.findCaseById(id);
            
            // Service가 검증할 수 있도록 요청 객체(DTO)에 진짜 비밀번호를 주입
            request.setGuestPw(realEntity.getGuestPw());
        }

        // 3. Service 호출 (관리자는 위에서 비밀번호를 맞춰줬으므로 무조건 통과됨)
        boolean isUpdated = service.updateCase(id, request);

        if (isUpdated) {
            return "<script>alert('수정되었습니다.'); location.href='/cases/" + id + "';</script>";
        } else {
            // 일반 사용자가 비밀번호를 틀렸을 때
            return "<script>alert('비밀번호가 일치하지 않습니다.'); history.back();</script>";
        }
    }

	// 5. [삭제] 관리자면 비밀번호 검증 패스
	@GetMapping("/cases/{id}/delete")
	@ResponseBody
	public String deleteCase(@PathVariable Long id, @RequestParam("pw") String inputPw, HttpServletRequest request) {

		FraudCaseEntity targetCase = service.findCaseById(id);
		int typeId = targetCase.getTypeId();

		Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
		boolean isDeleted = false;

		if (isAdmin != null && isAdmin) {
            String realPassword = targetCase.getGuestPw();
			isDeleted = service.deleteCase(id, realPassword);
		} else {
			// 일반 사용자: 입력받은 비밀번호로 시도
			isDeleted = service.deleteCase(id, inputPw);
		}
		
		if (isDeleted) {
			String redirectUrl = "/cases";
            if (typeId == 2) redirectUrl = "/cases/gov";
            else if (typeId == 3) redirectUrl = "/cases/tele";
            else if (typeId == 4) redirectUrl = "/cases/finance";

			return "<script>" + "alert('삭제되었습니다.');" + "location.href='" + redirectUrl + "';" + "</script>";
		} else {
			return "<script>" + "alert('비밀번호가 일치하지 않습니다.');" + "history.back();" + "</script>";
		}
	}
}