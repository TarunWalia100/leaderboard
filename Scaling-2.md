# Tiered Leaderboard System: Redis + Cassandra

A scalable leaderboard architecture that handles billions of players with sub-10ms latency for 99% of queries.

## Architecture

```
┌─────────────────────────────────────┐
│  Tier 1: Elite (Redis Sorted Set)  │
│  - Top 100K players                 │
│  - Exact rankings                   │
│  - 1ms latency                      │
│  - Memory: 1GB                      │
└─────────────────────────────────────┘
                +
┌─────────────────────────────────────┐
│  Tier 2: Regular (Cassandra)        │
│  - All 1B+ players                  │
│  - Percentile rankings              │
│  - 10ms latency                     │
│  - Scales horizontally              │
└─────────────────────────────────────┘
```

## How It Works

**Write:** Update score for player
1. Write to Cassandra (source of truth)
2. If score ≥ 100Kth rank threshold → add to Redis elite tier
3. Trim Redis to maintain exactly 100K players

**Read:** Get player rank
1. Check Redis elite tier (1ms)
2. If found → return exact rank: "You're #45,234"
3. If not found → query Cassandra for percentile (10ms): "You're in top 15.3%"

**Leaderboard:** Get top 100
- Always read from Redis elite tier (1ms)

## Performance

| Operation | Elite Players | Regular Players |
|-----------|---------------|-----------------|
| Add score | 2ms | 2ms |
| Get rank | 1ms (exact) | 10ms (percentile) |
| Top 100 | 1ms | 1ms |
| Scalability | 100K | Billions |

## Pros

✅ Scales infinitely (handles billions of players)  
✅ Fast where it matters (elite players get 1ms exact ranks)  
✅ Simple architecture (single Redis key, straightforward Cassandra table)  
✅ Cost-effective ($600/mo for 1B players vs $5K+ for alternatives)  
✅ Great UX (top players see rank, others see percentile - both are happy)

## Cons

❌ Dual system (Redis + Cassandra to maintain)  
❌ Not exact for 99.9% of players (percentile only)  
❌ Sync complexity (must keep both systems aligned)  
❌ Elite threshold fluctuates (players move in/out of top 100K)

## When to Use

✅ Gaming leaderboards (most use cases)  
✅ Social app rankings  
✅ 10M+ players  
✅ Users okay with "top 5%" for non-elite players

❌ Financial leaderboards (need exact ranks for all)  
❌ < 10M players (single Redis sufficient)  
❌ Regulatory requirements for exact rankings

## Technology Stack

- **Redis:** Single instance or cluster, 1-2GB RAM
- **Cassandra:** 3+ node cluster, distributed
- **Spring Boot:** Application layer with dual database support

---

**Bottom line:** Best balance of simplicity, cost, and scale for 95% of leaderboard use cases.