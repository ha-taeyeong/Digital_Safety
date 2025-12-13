package com.digital.safety.fraudCaseManagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GAME_SCORE")
public class GameScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NICKNAME", length = 10)
    private String nickname;

    @Column(name = "SCORE")
    private int score;

    @Column(name = "PLAYED_AT")
    private LocalDateTime playedAt;

    @PrePersist
    public void prePersist() {
        this.playedAt = LocalDateTime.now();
    }

    // --- Getter & Setter 메서드 직접 구현 ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }
}