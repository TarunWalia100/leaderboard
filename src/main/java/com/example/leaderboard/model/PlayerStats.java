package com.example.leaderboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStats {
    private String playerId;
    private Integer rank;
    private Double score;
    private Long totalPlayers;
}