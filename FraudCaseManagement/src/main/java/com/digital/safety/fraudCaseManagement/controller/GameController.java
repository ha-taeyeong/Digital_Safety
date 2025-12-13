package com.digital.safety.fraudCaseManagement.controller;

import com.digital.safety.fraudCaseManagement.entity.GameScoreEntity;
import com.digital.safety.fraudCaseManagement.repository.GameScoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class GameController {

    private final GameScoreRepository gameScoreRepository;

    // 생성자를 통해 Repository 주입 (DB 사용을 위함)
    public GameController(GameScoreRepository gameScoreRepository) {
        this.gameScoreRepository = gameScoreRepository;
    }

    // 1. 게임 화면 보여주기 (View)
    @GetMapping("/game")
    public String viewGame() {
        return "game"; // templates/game.html
    }

    // ==========================================
    //  [API] 게임 데이터 및 점수 관련 (JSON 반환)
    // ==========================================

    // 2. 피싱 키워드 목록 제공
    @GetMapping("/api/game/keywords")
    @ResponseBody
    public List<String> getGameKeywords() {
        List<String> keywords = Arrays.asList(
            "고수익보장", "선입금요구", "문화상품권", "계좌비밀번호", 
            "원격제어앱", "가족사칭", "검찰청", "금융감독원", 
            "저금리대출", "신분증요구", "기프트카드", "즉시출금",
            "해외결제", "통장대여", "카드정보", "보안카드",
            "대출상담", "신용등급", "현금전달", "명의도용"
        );
        Collections.shuffle(keywords);
        return keywords;
    }

    // 3. [DB] 점수 저장 (POST)
    @PostMapping("/api/game/score")
    @ResponseBody
    public ResponseEntity<String> saveScore(@RequestBody Map<String, Object> payload) {
        try {
            String nickname = (String) payload.get("nickname");
            int score = (Integer) payload.get("score");

            GameScoreEntity entity = new GameScoreEntity();
            entity.setNickname(nickname);
            entity.setScore(score);
            
            gameScoreRepository.save(entity); // DB에 저장
            
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 4. [DB] 랭킹 조회 (GET) - Top 5
    @GetMapping("/api/game/ranking")
    @ResponseBody
    public ResponseEntity<List<GameScoreEntity>> getRanking() {
        // 점수 높은 순으로 상위 5개 가져오기
        return ResponseEntity.ok(gameScoreRepository.findTop5ByOrderByScoreDesc());
    }
}