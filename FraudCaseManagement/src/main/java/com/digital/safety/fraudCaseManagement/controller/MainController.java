package com.digital.safety.fraudCaseManagement.controller;

import com.digital.safety.fraudCaseManagement.dto.FraudCaseResponse;
import com.digital.safety.fraudCaseManagement.service.FraudCaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private final FraudCaseService service;

    public MainController(FraudCaseService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(Model model) {
        // 1. 리스트 데이터 (기존 동일)
        model.addAttribute("recentJobCases", getRecentCases(1));
        model.addAttribute("recentGovCases", getRecentCases(2));
        model.addAttribute("recentTeleCases", getRecentCases(3));
        model.addAttribute("recentFinanceCases", getRecentCases(4));

        // 2. [수정] 차트 데이터: DB 기반 실시간 통계 가져오기
        Map<String, Object> stats = service.getFraudStatistics();
        
        model.addAttribute("chartLabels", stats.get("labels"));
        model.addAttribute("chartData", stats.get("data"));

        return "main";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * [Helper Method] 특정 유형의 최신 사례 4개를 조회하는 메서드
     * 반복되는 코드를 줄이기 위해 추출했습니다.
     */
    private List<FraudCaseResponse> getRecentCases(int typeId) {
        // 1. 전체 조회 (Service 호출)
        List<FraudCaseResponse> allCases = service.findAllByTypeId(typeId);

        // 2. 최신순 4개 자르기 (Stream API)
        return allCases.stream()
                .limit(4) // 화면 디자인에 맞춰 개수 조절 (3~5개 권장)
                .collect(Collectors.toList());
    }
    
    
}