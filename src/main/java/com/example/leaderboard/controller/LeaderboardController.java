package com.example.leaderboard.controller;

import com.example.leaderboard.model.PlayerScore;
import com.example.leaderboard.model.PlayerStats;
import com.example.leaderboard.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    
    private final LeaderboardService leaderboardService;
    
    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }
    
    /**
     * POST /api/leaderboard/score
     * Add or update player score
     * Body: { "playerId": "player1", "score": 1000 }
     */
    @PostMapping("/score")
    public ResponseEntity<String> addScore(@RequestBody Map<String, Object> request) {
        String playerId = (String) request.get("playerId");
        double score = ((Number) request.get("score")).doubleValue();
        leaderboardService.addScore(playerId, score);
        return ResponseEntity.ok("Score added successfully");
    }
    
    /**
     * POST /api/leaderboard/increment
     * Increment player score
     * Body: { "playerId": "player1", "points": 50 }
     */
    @PostMapping("/increment")
    public ResponseEntity<Double> incrementScore(@RequestBody Map<String, Object> request) {
        String playerId = (String) request.get("playerId");
        double points = ((Number) request.get("points")).doubleValue();
        Double newScore = leaderboardService.incrementScore(playerId, points);
        return ResponseEntity.ok(newScore);
    }
    
    /**
     * GET /api/leaderboard/top?count=10
     * Get top N players (without scores)
     */
    @GetMapping("/top")
    public ResponseEntity<List<String>> getTopPlayers(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(leaderboardService.getTopPlayers(count));
    }
    
    /**
     * GET /api/leaderboard/top-with-scores?count=10
     * Get top N players with scores
     */
    @GetMapping("/top-with-scores")
    public ResponseEntity<List<PlayerScore>> getTopPlayersWithScores(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(leaderboardService.getTopPlayersWithScores(count));
    }
    
    /**
     * GET /api/leaderboard/player/{playerId}/rank
     * Get player's rank
     */
    @GetMapping("/player/{playerId}/rank")
    public ResponseEntity<Integer> getPlayerRank(@PathVariable String playerId) {
        Integer rank = leaderboardService.getPlayerRank(playerId);
        return rank != null ? ResponseEntity.ok(rank) : ResponseEntity.notFound().build();
    }
    
    /**
     * GET /api/leaderboard/player/{playerId}/score
     * Get player's score
     */
    @GetMapping("/player/{playerId}/score")
    public ResponseEntity<Double> getPlayerScore(@PathVariable String playerId) {
        Double score = leaderboardService.getPlayerScore(playerId);
        return score != null ? ResponseEntity.ok(score) : ResponseEntity.notFound().build();
    }
    
    /**
     * GET /api/leaderboard/player/{playerId}/stats
     * Get player's complete stats
     */
    @GetMapping("/player/{playerId}/stats")
    public ResponseEntity<PlayerStats> getPlayerStats(@PathVariable String playerId) {
        PlayerStats stats = leaderboardService.getPlayerStats(playerId);
        return stats.getRank() != null ? ResponseEntity.ok(stats) : ResponseEntity.notFound().build();
    }
    
    /**
     * GET /api/leaderboard/player/{playerId}/around?count=5
     * Get players around a specific player
     */
    @GetMapping("/player/{playerId}/around")
    public ResponseEntity<List<PlayerScore>> getPlayersAround(
            @PathVariable String playerId,
            @RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(leaderboardService.getPlayersAround(playerId, count));
    }
    
    /**
     * DELETE /api/leaderboard/player/{playerId}
     * Remove player from leaderboard
     */
    @DeleteMapping("/player/{playerId}")
    public ResponseEntity<String> removePlayer(@PathVariable String playerId) {
        leaderboardService.removePlayer(playerId);
        return ResponseEntity.ok("Player removed successfully");
    }
    
    /**
     * GET /api/leaderboard/total
     * Get total number of players
     */
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalPlayers() {
        return ResponseEntity.ok(leaderboardService.getTotalPlayers());
    }
    
    /**
     * GET /api/leaderboard/range?min=1000&max=2000
     * Get players within score range
     */
    @GetMapping("/range")
    public ResponseEntity<List<PlayerScore>> getPlayersByScoreRange(
            @RequestParam double min,
            @RequestParam double max) {
        return ResponseEntity.ok(leaderboardService.getPlayersByScoreRange(min, max));
    }
}