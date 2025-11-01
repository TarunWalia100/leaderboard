package com.example.leaderboard.service;

import com.example.leaderboard.model.PlayerScore;
import com.example.leaderboard.model.PlayerStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LeaderboardService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetOperations<String, Object> zSetOps;
    
    @Value("${leaderboard.key}")
    private String leaderboardKey;
    
    public LeaderboardService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }
    
    /**
     * Add or update player score
     */
    public void addScore(String playerId, double score) {
        zSetOps.add(leaderboardKey, playerId, score);
    }
    
    /**
     * Increment player score
     */
    public Double incrementScore(String playerId, double points) {
        return zSetOps.incrementScore(leaderboardKey, playerId, points);
    }
    
    /**
     * Get top N players (without scores)
     */
    public List<String> getTopPlayers(int count) {
        Set<Object> topPlayers = zSetOps.reverseRange(leaderboardKey, 0, count - 1);
        return topPlayers != null ? topPlayers.stream()
                .map(Object::toString)
                .toList() : new ArrayList<>();
    }
    
    /**
     * Get top N players with scores
     */
    public List<PlayerScore> getTopPlayersWithScores(int count) {
        Set<ZSetOperations.TypedTuple<Object>> topPlayersWithScores = 
                zSetOps.reverseRangeWithScores(leaderboardKey, 0, count - 1);
        
        List<PlayerScore> result = new ArrayList<>();
        if (topPlayersWithScores != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> tuple : topPlayersWithScores) {
                result.add(new PlayerScore(
                    rank++,
                    tuple.getValue().toString(),
                    tuple.getScore()
                ));
            }
        }
        return result;
    }
    
    /**
     * Get player rank (1-indexed)
     */
    public Integer getPlayerRank(String playerId) {
        Long rank = zSetOps.reverseRank(leaderboardKey, playerId);
        return rank != null ? rank.intValue() + 1 : null;
    }
    
    /**
     * Get player score
     */
    public Double getPlayerScore(String playerId) {
        return zSetOps.score(leaderboardKey, playerId);
    }
    
    /**
     * Get player stats (rank, score, total players)
     */
    public PlayerStats getPlayerStats(String playerId) {
        Integer rank = getPlayerRank(playerId);
        Double score = getPlayerScore(playerId);
        Long totalPlayers = zSetOps.zCard(leaderboardKey);
        
        return new PlayerStats(playerId, rank, score, totalPlayers);
    }
    
    /**
     * Get players around a specific player
     */
    public List<PlayerScore> getPlayersAround(String playerId, int count) {
        Long rank = zSetOps.reverseRank(leaderboardKey, playerId);
        if (rank == null) {
            return new ArrayList<>();
        }
        
        long start = Math.max(0, rank - count);
        long end = rank + count;
        
        Set<ZSetOperations.TypedTuple<Object>> playersAround = 
                zSetOps.reverseRangeWithScores(leaderboardKey, start, end);
        
        List<PlayerScore> result = new ArrayList<>();
        if (playersAround != null) {
            int currentRank = (int) start + 1;
            for (ZSetOperations.TypedTuple<Object> tuple : playersAround) {
                result.add(new PlayerScore(
                    currentRank++,
                    tuple.getValue().toString(),
                    tuple.getScore()
                ));
            }
        }
        return result;
    }
    
    /**
     * Remove player from leaderboard
     */
    public void removePlayer(String playerId) {
        zSetOps.remove(leaderboardKey, playerId);
    }
    
    /**
     * Get total number of players
     */
    public Long getTotalPlayers() {
        return zSetOps.zCard(leaderboardKey);
    }
    
    /**
     * Get players in score range
     */
    public List<PlayerScore> getPlayersByScoreRange(double minScore, double maxScore) {
        Set<ZSetOperations.TypedTuple<Object>> players = 
                zSetOps.reverseRangeByScoreWithScores(leaderboardKey, minScore, maxScore);
        
        List<PlayerScore> result = new ArrayList<>();
        if (players != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : players) {
                Long rank = zSetOps.reverseRank(leaderboardKey, tuple.getValue());
                result.add(new PlayerScore(
                    rank != null ? rank.intValue() + 1 : null,
                    tuple.getValue().toString(),
                    tuple.getScore()
                ));
            }
        }
        return result;
    }
}