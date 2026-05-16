# Ranking Performance

The legacy ranking endpoints loaded every daily `record`, calculated the ranking in memory, sorted the full list, and returned the whole response.

This keeps the legacy no-query-parameter behavior for compatibility, but adds a paged response path on the same endpoints:

```text
GET /core/rank/black?page=0&size=50
GET /core/rank/guild?page=0&size=50
```

## Flow

```text
record table
-> RankService refresh
-> rank_snapshot table
-> indexed page query
-> short application cache
-> API response
```

The scheduled account sync refreshes `rank_snapshot` after updating the daily records. If a snapshot is missing or stale, the first ranking request also rebuilds it.

## Why Not Redis First

The first bottleneck was not distributed cache coordination. It was request-time full-table ranking work and full-list rendering. The current improvement removes that work from hot requests first.

Redis is a good next step when the service runs multiple app instances or needs shared cache invalidation between blue-green containers. Until then, the local page cache keeps the architecture simpler while the database snapshot handles the main load reduction.

## Runtime Settings

```text
RANK_PAGE_DEFAULT_SIZE=50
RANK_PAGE_MAX_SIZE=200
RANK_CACHE_TTL_SECONDS=60
RANK_SNAPSHOT_MAX_AGE_SECONDS=900
```

## Metrics

The service emits Micrometer counters:

```text
rank_cache_requests_total{cache="hit|miss", mode="page|legacy"}
rank_snapshot_refresh_total
```

These are available from `/actuator/prometheus` and can be graphed in Grafana.
