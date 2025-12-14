package com.digital.safety.fraudCaseManagement.controller;

import com.digital.safety.fraudCaseManagement.dto.FraudCaseRequest;
import com.digital.safety.fraudCaseManagement.dto.FraudCaseResponse;
import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import com.digital.safety.fraudCaseManagement.service.FraudCaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class FraudCaseViewController {

    private final FraudCaseService service;

    public FraudCaseViewController(FraudCaseService service) {
        this.service = service;
    }

    // --- [조회 기능] ---

    // 1. 구인 사기 목록 (Type ID: 1)
    @GetMapping("/cases")
    public String viewJobFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
        Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(1, pageable);
        
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);
        
        model.addAttribute("cases", pagingResult);
        model.addAttribute("categoryName", "구인 사기");
        model.addAttribute("typeId", 1);
        return "scam_cases";
    }

    // 2. 정부/기관 사칭 목록 (Type ID: 2)
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

    // 3. 텔레그램 사기 목록 (Type ID: 3)
    @GetMapping("/cases/tele")
    public String viewTeleFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
        Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(3, pageable);
        
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);

        model.addAttribute("cases", pagingResult);
        model.addAttribute("categoryName", "텔레그램 사칭");
        model.addAttribute("typeId", 3);
        
        return "tele_list"; 
    }

    // 4. 금융 사기 목록 (Type ID: 4)
    @GetMapping("/cases/finance")
    public String viewFinanceFraudList(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regiDt").descending());
        Page<FraudCaseResponse> pagingResult = service.findAllByTypeId(4, pageable);
        
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin != null && isAdmin);

        model.addAttribute("cases", pagingResult);
        model.addAttribute("categoryName", "금융/대출 사기");
        model.addAttribute("typeId", 4);
        
        return "finance_list";
    }

    // 상세 조회
    @GetMapping("/cases/{id}")
    public String viewDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        service.increaseViewCount(id);
        FraudCaseEntity entity = service.findCaseById(id);
        
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
        if (typeId == 2) return "redirect:/cases/gov";
        else if (typeId == 3) return "redirect:/cases/tele";
        else if (typeId == 4) return "redirect:/cases/finance";
        else return "redirect:/cases";
    }

    // [수정 폼 진입 - GET]
    @GetMapping("/cases/{id}/edit")
    public String viewEditForm(@PathVariable Long id, @RequestParam("pw") String inputPw, Model model,
                               HttpServletRequest request) {

        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        boolean isPass = false;

        if (isAdmin != null && isAdmin) {
            isPass = true; 
        } else {
            if (service.verifyPassword(id, inputPw)) {
                isPass = true;
            }
        }

        // 비밀번호 불일치 시 알림창으로 이동
        if (!isPass) {
            model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
            return "message"; 
        }

        FraudCaseEntity entity = service.findCaseById(id);
        model.addAttribute("case", entity);
        model.addAttribute("guestPw", inputPw);
        return "edit-form";
    }

    // [수정 완료 처리 - POST] 
    @PostMapping("/cases/{id}/edit")
    public String updateCase(@PathVariable Long id, FraudCaseRequest request, HttpServletRequest httpRequest, Model model) {
        
        Boolean isAdmin = (Boolean) httpRequest.getSession().getAttribute("isAdmin");
        if (isAdmin != null && isAdmin) {
            FraudCaseEntity realEntity = service.findCaseById(id);
            request.setGuestPw(realEntity.getGuestPw());
        }

        boolean isUpdated = service.updateCase(id, request);

        if (isUpdated) {
            model.addAttribute("message", "성공적으로 수정되었습니다.");
            model.addAttribute("searchUrl", "/cases/" + id);
        } else {
            model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
        }
        
        return "message"; 
    }

    // [삭제 처리 - GET]
    @GetMapping("/cases/{id}/delete")
    public String deleteCase(@PathVariable Long id, @RequestParam("pw") String inputPw, HttpServletRequest request, Model model) {

        FraudCaseEntity targetCase = service.findCaseById(id);
        int typeId = targetCase.getTypeId();

        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        boolean isDeleted = false;

        if (isAdmin != null && isAdmin) {
            isDeleted = service.deleteCase(id, targetCase.getGuestPw());
        } else {
            isDeleted = service.deleteCase(id, inputPw);
        }
        
        if (isDeleted) {
            String redirectUrl = "/cases";
            if (typeId == 2) redirectUrl = "/cases/gov";
            else if (typeId == 3) redirectUrl = "/cases/tele";
            else if (typeId == 4) redirectUrl = "/cases/finance";

            model.addAttribute("message", "성공적으로 삭제되었습니다.");
            model.addAttribute("searchUrl", redirectUrl);
        } else {
            model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
        }

        return "message"; 
    }
}