package com.digital.safety.fraudCaseManagement.controller;

import com.digital.safety.fraudCaseManagement.dto.FraudCaseRequest;
import com.digital.safety.fraudCaseManagement.dto.FraudCaseResponse;
import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import com.digital.safety.fraudCaseManagement.service.FraudCaseService;
import jakarta.servlet.http.HttpServletResponse;
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
	public String viewJobFraudList(@RequestParam(defaultValue = "0") int page, Model model) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
		Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(1, pageable);
		model.addAttribute("cases", pagingResult);
		model.addAttribute("categoryName", "구인 사기");
		model.addAttribute("typeId", 1);
		return "scam_cases";
	}

	@GetMapping("/cases/gov")
	public String viewGovFraudList(@RequestParam(defaultValue = "0") int page, Model model) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
		Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(2, pageable);
		model.addAttribute("cases", pagingResult);
		model.addAttribute("categoryName", "정부, 공공기관 사칭");
		model.addAttribute("typeId", 2);
		return "gov_list";
	}

	@GetMapping("/cases/tele")
	public String viewTeleFraudList(@RequestParam(defaultValue = "0") int page, Model model) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
		Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(3, pageable);
		model.addAttribute("cases", pagingResult);
		model.addAttribute("categoryName", "텔레그램 사칭");
		model.addAttribute("typeId", 3);
		return "tele_list";
	}

	@GetMapping("/cases/finance")
	public String viewFinanceFraudList(@RequestParam(defaultValue = "0") int page, Model model) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
		Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(4, pageable);
		model.addAttribute("cases", pagingResult);
		model.addAttribute("categoryName", "금융기관 사칭");
		model.addAttribute("typeId", 4);
		return "finance_list";
	}

	@GetMapping("/cases/{id}")
	public String viewDetail(@PathVariable Long id, Model model) {
	    // 1. 조회수 증가 로직 (정상)
	    service.increaseViewCount(id); 
	    
	    // 2. 증가된 조회수를 포함한 최신 엔티티 조회 (정상)
	    FraudCaseEntity entity = service.findCaseById(id);
	    model.addAttribute("case", entity);
	    
	    return "detail";
	}

	// --- [CUD 기능 : 비밀번호 검증] ---

	// 1. 등록 폼
	@GetMapping("/cases/new")
	public String viewAddForm(@RequestParam(required = false, defaultValue = "1") Integer type, Model model) {
		model.addAttribute("targetType", type);
		return "add-form";
	}

	// 2. 저장
	@PostMapping("/cases/save")
	public String saveCase(FraudCaseRequest request) {
		service.createCase(request);

		int typeId = request.getTypeId();
		if (typeId == 2)
			return "redirect:/cases/gov";
		else if (typeId == 3)
			return "redirect:/cases/tele";
		else if (typeId == 4)
			return "redirect:/cases/finance";
		else
			return "redirect:/cases";
	}

	// 3. 수정 폼 진입 (비밀번호 1차 검증)
	@GetMapping("/cases/{id}/edit")
	public String viewEditForm(@PathVariable Long id, @RequestParam("pw") String inputPw, Model model,
			HttpServletResponse response) throws Exception {

		if (!service.verifyPassword(id, inputPw)) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>alert('비밀번호가 일치하지 않습니다.'); history.back();</script>");
			out.flush();
			return null;
		}

		FraudCaseEntity entity = service.findCaseById(id);
		model.addAttribute("case", entity);
		model.addAttribute("guestPw", inputPw);
		return "edit-form";
	}

	// 4. 수정 완료 (비밀번호 2차 검증)
	@PostMapping("/cases/{id}/edit")
	@ResponseBody
	public String updateCase(@PathVariable Long id, FraudCaseRequest request) {
		boolean isUpdated = service.updateCase(id, request);

		if (isUpdated) {
			return "<script>alert('수정되었습니다.'); location.href='/cases/" + id + "';</script>";
		} else {
			return "<script>alert('비밀번호가 일치하지 않습니다.'); history.back();</script>";
		}
	}

	// [기존 코드의 문제점]
	// 성공 시 무조건 location.href='/cases'로 이동함 -> 구인사기 목록으로 감

	// [수정된 코드]
	// 1. 삭제 전 게시글 정보를 조회해서 typeId를 알아냄
	// 2. typeId에 따라 알맞은 리스트 페이지로 이동시킴

	@GetMapping("/cases/{id}/delete")
	@ResponseBody
	public String deleteCase(@PathVariable Long id, @RequestParam("pw") String inputPw) {

		// 1. 삭제하려는 게시글이 어떤 게시판 글인지 먼저 조회 (삭제 후엔 조회가 안 되므로 먼저 해야 함)
		FraudCaseEntity targetCase = service.findCaseById(id);
		int typeId = targetCase.getTypeId();

		// 2. 삭제 시도
		boolean isDeleted = service.deleteCase(id, inputPw);

		// 3. 결과 처리
		if (isDeleted) {
			// 원래 있던 게시판으로 돌아가기 위한 URL 설정
			String redirectUrl = "/cases"; // 기본값 (구인사기)

			if (typeId == 2) {
				redirectUrl = "/cases/gov"; // 정부 사칭
			} else if (typeId == 3) {
				redirectUrl = "/cases/tele"; // 텔레그램 사칭
			} else if (typeId == 4) {
				redirectUrl = "/cases/finance"; // 금융 사칭
			}

			return "<script>" + "alert('삭제되었습니다.');" + "location.href='" + redirectUrl + "';" + "</script>";
		} else {
			return "<script>" + "alert('비밀번호가 일치하지 않습니다.');" + "history.back();" + "</script>";
		}
	}
}