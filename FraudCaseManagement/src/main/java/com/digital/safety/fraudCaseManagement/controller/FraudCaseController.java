package com.digital.safety.fraudCaseManagement.controller;

import com.digital.safety.fraudCaseManagement.dto.FraudCaseRequest;
import com.digital.safety.fraudCaseManagement.dto.FraudCaseResponse;
import com.digital.safety.fraudCaseManagement.entity.FraudCaseEntity;
import com.digital.safety.fraudCaseManagement.service.FraudCaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
public class FraudCaseController {

	private final FraudCaseService service;

	public FraudCaseController(FraudCaseService service) {
		this.service = service;
	}

	// 1. 등록 API
	@PostMapping
	public ResponseEntity<Map<String, Object>> addFraudCase(@RequestBody FraudCaseRequest request) {
		try {
			Long newCaseId = service.createCase(request);

			Map<String, Object> response = new HashMap<>();
			response.put("message", "등록 완료");
			response.put("id", newCaseId);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "등록 실패", "details", e.getMessage()));
		}
	}

	// 2. 목록 조회 API
	@GetMapping
	public ResponseEntity<List<FraudCaseResponse>> findAllFraudCases(@RequestParam(defaultValue = "1") Integer typeId) {
		return ResponseEntity.ok(service.findAllByTypeId(typeId));
	}

	// 3. 상세 조회 API
	@GetMapping("/{caseId}")
	public ResponseEntity<FraudCaseResponse> findCaseDetail(@PathVariable Long caseId) {
		try {
			FraudCaseEntity entity = service.findCaseById(caseId);
			return ResponseEntity.ok(new FraudCaseResponse(entity));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// 4. 수정 API (★비밀번호 검증★)
	@PutMapping("/{caseId}")
	public ResponseEntity<String> updateFraudCase(@PathVariable Long caseId, @RequestBody FraudCaseRequest request) {
		// Service의 updateCase가 boolean을 반환함
		boolean isUpdated = service.updateCase(caseId, request);

		if (isUpdated) {
			return ResponseEntity.ok("수정 완료");
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호 불일치");
		}
	}

	// 5. 삭제 API (★비밀번호 검증★)
	@DeleteMapping("/{caseId}")
	public ResponseEntity<String> deleteFraudCase(@PathVariable Long caseId, @RequestParam("pw") String inputPw) {
		// [수정됨] 메서드 이름을 service에 맞춰 deleteCase로 변경
		boolean isDeleted = service.deleteCase(caseId, inputPw);

		if (isDeleted) {
			return ResponseEntity.ok("삭제 완료");
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호 불일치");
		}
	}

	// 6. 좋아요 토글 API
	@PostMapping("/like/{caseId}")
	public ResponseEntity<Map<String, Integer>> toggleLike(@PathVariable Long caseId,
			@RequestBody Map<String, Integer> request) {
		try {
			Integer change = request.get("change");
			if (change == null || (change != 1 && change != -1)) {
				return ResponseEntity.badRequest().build();
			}

			// Service에서 좋아요 토글 처리 및 새 카운트 반환 (Service 로직은 이미 구현되어 있음)
			int newCount = service.toggleLike(caseId, change);

			Map<String, Integer> response = new HashMap<>();
			response.put("newCount", newCount);

			// 클라이언트(JS)는 이 JSON 객체를 받아서 화면을 업데이트합니다.
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}