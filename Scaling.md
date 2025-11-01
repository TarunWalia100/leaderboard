# Global Leaderboard at Scale: The Async Cache Pattern

## The Problem

**Scenario:** 100M+ players, sharded across 10 Redis nodes

```
User Request: "Show me global top 100"

Naive Approach (Real-time Scatter-Gather):
├─ Query all 10 shards in parallel
├─ Each returns top 100 → 1000 results total
├─ Merge & sort 1000 results in application
└─ Return top 100

Performance: 10-20ms per request
At 10k requests/sec: 10k × 10 shard queries = 100k queries/sec to Redis!
Network traffic: 10MB/sec just for leaderboard queries
CPU overhead: Constant merging in application layer
```

**Scale Issues:**
- High latency (10-20ms vs 1ms for single-node queries)
- Network amplification (1 user query → 10 Redis queries)
- Wasted resources (same top 100 calculated millions of times)
- Redis cluster overload during peak hours

## The Solution: Async Global Cache

```
┌─────────────────────────────────────────────────┐
│  Background Job (every 60 seconds)              │
│  1. Scatter-gather from all 10 shards           │
│  2. Merge results → global top 1000             │
│  3. Store in "leaderboard:global:cache"         │
└─────────────────────────────────────────────────┘
                      ↓
              ┌───────────────┐
              │  Cache (1 key)│
              │  Master Node  │
              └───────────────┘
                      ↑
┌─────────────────────┴──────────────────────────┐
│  User Requests (10k/sec)                       │
│  Read from cache → 1ms                         │
│  NO scatter-gather!                            │
└────────────────────────────────────────────────┘
```

## Pros

✅ **Fast Reads:** 1ms (single Redis query, no scatter-gather)
✅ **Scalable:** 100k+ requests/sec (reads from one node)
✅ **Low Resource Usage:** Scatter-gather happens once/minute, not per request
✅ **Simple Code:** User-facing queries just read one key
✅ **Network Efficient:** 1 user query → 1 Redis query (not 10)

## Cons

❌ **Eventual Consistency:** Cache may be up to 60 seconds stale
❌ **Background Load:** Scatter-gather job creates periodic spikes
❌ **Memory Cost:** Cache duplicates top N players (usually negligible)
❌ **Not Real-Time:** Player won't see rank change immediately in global view

## When This Works Best

- **Acceptable Staleness:** Users don't need exact real-time global rankings
- **High Read/Write Ratio:** 1000:1 or higher (typical for leaderboards)
- **Top-Heavy Queries:** Users mostly care about top 100-1000, not their exact global rank
- **Peak Traffic:** Need to handle 10k+ requests/sec without degradation

## Alternative for Critical Real-Time Needs

If 60-second staleness is unacceptable, consider:
- **Shorter refresh:** 5-10 seconds (higher load, but fresher)
- **Incremental updates:** Only update cache when top 1000 changes
- **Single global key:** Accept RAM limit (~100M players max on one node)
- **Different data model:** Show regional ranks (real-time) + global rank (cached)