# Rate Limiter Service


A production-grade distributed rate limiting service built with Java 17 and Spring Boot 4.1.0.
Supports multiple rate limiting algorithms (Token Bucket, Sliding Window Log) with both in-memory
and Redis-backed distributed implementations. Includes Prometheus monitoring and Grafana dashboards
for real-time observability — entire stack runs with a single `docker-compose up`.


---


## Architecture Overview


```
[Client Request]
        │
        ▼
[Rate Limiter Service]
        │
    ┌───┴───────────────────────┐
    │                           │
[In-Memory Limiter]      [Distributed Limiter]
│                           │
│ - Token Bucket           │ - Redis-backed state
│ - Sliding Window Log     │ - Token Bucket
│ - ConcurrentHashMap       │ - Lua scripts for atomicity
│                           │
    └───────────────────────────┘
                │
                ▼
        [Rate Limit Decision]
                │
        ┌───────┴───────┐
        │               │
   [ALLOWED]      [BLOCKED]
        │               │
        ▼               ▼
[Resource]      [429 Response]
```


---


## Tech Stack


| Layer | Technology | Purpose |
|---|---|---|
| Application Framework | Java 17 + Spring Boot 4.1.0 | REST API, dependency injection |
| Rate Limiting Algorithms | Token Bucket, Sliding Window Log | Flexible rate limiting strategies |
| Distributed State | Redis 7 (Alpine) | Shared state across instances |
| Monitoring | Spring Boot Actuator + Micrometer | Metrics collection |
| Metrics Backend | Prometheus | Time-series database |
| Visualization | Grafana 11.3.0 | Real-time dashboards |
| Containerization | Docker + Docker Compose | One-command setup |


---


## Project Structure


```
rate-limiter-service/
├── src/main/java/com/ratelimiter/rate_limiter_service/
│   ├── RateLimiterServiceApplication.java
│   ├── inmemory/
│   │   ├── config/
│   │   │   ├── AlgorithmConfig.java
│   │   │   └── RateLimiterStoreConfig.java
│   │   ├── controller/
│   │   │   ├── RateLimiterController.java
│   │   │   └── RateLimiterConfigController.java
│   │   ├── dto/
│   │   │   └── RateLimitResult.java
│   │   ├── enums/
│   │   │   └── RateLimiterType.java
│   │   ├── factory/
│   │   │   └── RateLimiterFactory.java
│   │   ├── manager/
│   │   │   └── RateLimiterManager.java
│   │   ├── model/
│   │   │   └── RateLimiterConfig.java
│   │   ├── ratelimiter/
│   │   │   ├── RateLimiter.java
│   │   │   ├── TokenBucketRateLimiter.java
│   │   │   └── SlidingWindowLogRateLimiter.java
│   │   └── service/
│   │       └── RateLimiterService.java
│   └── distributed/
│       ├── config/
│       │   ├── AlgorithmConfig.java
│       │   ├── RedisConfig.java
│       │   └── RedisRateLimiterStoreConfig.java
│       ├── controller/
│       │   ├── RateLimiterController.java
│       │   └── RedisRateLimiterConfigController.java
│       ├── dto/
│       │   └── RateLimitResult.java
│       ├── enums/
│       │   └── RateLimiterType.java
│       ├── manager/
│       │   └── RedisRateLimiterManager.java
│       ├── model/
│       │   └── RateLimiterConfig.java
│       ├── ratelimiter/
│       │   ├── RateLimiter.java
│       │   └── TokenBucketAlgorithm.java
│       └── service/
│           └── RedisRateLimiter.java
├── src/main/resources/
│   ├── application.properties
│   └── lua/
│       ├── fixed_window.lua
│       ├── leaky_bucket.lua
│       ├── sliding_window_counter.lua
│       ├── sliding_window_log.lua
│       └── token_bucket.lua
├── src/test/java/
├── k6/
│   ├── rate-limiter-test.js
│   ├── load-test-1k.js
│   ├── load-test-5k.js
│   └── load-test-10k.js
├── prometheus/
│   └── prometheus.yml
├── grafana/
│   └── provisioning/
├── pom.xml
├── docker-compose.yml
├── Dockerfile
└── README.md
```


---


## Prerequisites


- [Docker Desktop](https://www.docker.com/products/docker-desktop/) — free for personal use (Windows, Mac, Linux)
- Java 17+ (for local development without Docker)
- Maven 3.6+ (for local development without Docker)


That's it. Docker handles Java, Redis, Prometheus, and Grafana — no manual installs needed.


---


## Quick Start


### 1. Clone the repo
```bash
git clone https://github.com/your-username/rate-limiter-service.git
cd rate-limiter-service
```


### 2. Start the Infrastructure
Start the entire stack (Rate Limiter Service, Redis, Prometheus, Grafana) using Docker Compose:
```bash
docker-compose up --build
```

Or run in detached mode:
```bash
docker-compose up -d --build
```


### 3. Verify the Service
Once all services are running, you can access:

- **Rate Limiter API:** http://localhost:8080
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (login: `admin` / `admin`)


---


## API Usage


### In-Memory Rate Limiting


#### Token Bucket Algorithm
```bash
curl -X GET http://localhost:8080/api/v1/resource/token-bucket \
  -H "X-Client-Id: client-123"
```

#### Sliding Window Log Algorithm
```bash
curl -X GET http://localhost:8080/api/v1/resource/sliding-window-log \
  -H "X-Client-Id: client-123"
```


### Distributed (Redis) Rate Limiting


```bash
curl -X GET http://localhost:8080/api/v1/redis/resource/token-bucket \
  -H "X-Client-Id: client-123"
```


### Response (Allowed)
```json
{
  "message": "request allowed",
  "algorithm": "TOKEN_BUCKET",
  "clientKey": "client-123"
}
```


### Response (Rate Limited)
```json
{
  "error": "rate_limit_exceeded",
  "algorithm": "TOKEN_BUCKET"
}
```


### Configuration Endpoints


#### Configure In-Memory Rate Limiter
```bash
curl -X POST http://localhost:8080/api/v1/config/{algorithm} \
  -H "Content-Type: application/json" \
  -d '{
    "maxRequests": 10,
    "windowInSeconds": 60
  }'
```

Example:
```bash
curl -X POST http://localhost:8080/api/v1/config/token-bucket \
  -H "Content-Type: application/json" \
  -d '{
    "maxRequests": 10,
    "windowInSeconds": 60
  }'
```


#### Configure Distributed Rate Limiter
```bash
curl -X POST http://localhost:8080/api/v1/redis/config/{algorithm} \
  -H "Content-Type: application/json" \
  -d '{
    "maxRequests": 10,
    "windowInSeconds": 60
  }'
```

Example:
```bash
curl -X POST http://localhost:8080/api/v1/redis/config/token-bucket \
  -H "Content-Type: application/json" \
  -d '{
    "maxRequests": 10,
    "windowInSeconds": 60
  }'
```


---


## Environment Variables


Configure the service via environment variables or `docker-compose.yml`:


```bash
# Redis Configuration
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

# Service Configuration
SERVER_PORT=8080
```


> Service names like `redis` are used as hostnames — Docker's internal DNS resolves them automatically between containers.


---


## Key Engineering Decisions


### Dual Implementation: In-Memory vs Distributed


The service provides both in-memory and Redis-backed implementations:
- **In-Memory:** Fast, single-instance deployments using `ConcurrentHashMap` for thread-safe state
- **Distributed:** Multi-instance deployments using Redis for shared state with Lua scripts for atomicity


### Algorithm Selection


Two production-ready algorithms are implemented:

**Token Bucket:**
- Allows bursts up to capacity
- Smooth rate limiting over time
- Memory efficient (O(1) per client)
- Suitable for APIs with bursty traffic patterns

**Sliding Window Log:**
- Precise request counting within the window
- No burst allowance beyond configured limit
- Memory intensive (O(n) per client where n = requests in window)
- Suitable for strict rate limiting requirements


### Client Key Resolution


The service resolves client identity in order:
1. `X-Client-Id` header (if provided)
2. Client IP address (fallback for anonymous requests)

This design allows tiered rate limiting (free vs paid tiers) once authentication is added, while still supporting anonymous users keyed by IP.


### Thread-Safe State Management


In-memory implementations use:
- `ConcurrentHashMap` for per-client state storage
- Synchronized blocks only on individual client buckets (not global lock)
- This minimizes contention while ensuring correctness for concurrent requests from the same client


---


## Monitoring


### Prometheus Metrics


The service exposes metrics at `/actuator/prometheus`:

- `http_server_requests_seconds` — Request latency
- `rate_limit_allowed_total` — Total allowed requests
- `rate_limit_denied_total` — Total denied requests
- `rate_limit_check_duration_seconds` — Rate limit check latency


### Grafana Dashboards


Pre-configured dashboards include:
- **Request Rate:** Requests per second by endpoint
- **Rate Limit Stats:** Allowed vs denied requests
- **Latency:** P50, P95, P99 latencies
- **Redis Operations:** Redis command statistics


---


## Load Testing


### Prerequisites


Install k6 (the modern load testing tool):
```bash
# macOS
brew install k6

# Linux
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# Windows
choco install k6

# Or download from https://k6.io/
```


### Load Test Scenarios


The project includes production-grade load testing scripts for different scale scenarios:


| Scenario | Concurrent Users | Duration | Purpose |
|---|---|---|---|
| 1K UV | 1,000 VUs | 5 minutes | Baseline performance testing |
| 5K UV | 5,000 VUs (ramped) | 15 minutes | Medium-scale production load |
| 10K UV | 10,000 VUs (ramped) | 22 minutes | High-scale production stress test |


### Running Load Tests


#### 1K UV Load Test (In-Memory)
```bash
cd k6
k6 run load-test-1k.js
```


#### 5K UV Load Test (Distributed/Redis)
```bash
cd k6
USE_REDIS=true k6 run load-test-5k.js
```


#### 10K UV Load Test (Production Stress Test)
```bash
cd k6
USE_REDIS=true ALGORITHM=token-bucket k6 run load-test-10k.js
```


### Custom Load Testing


You can customize the base script with environment variables:


```bash
cd k6

# Custom VUs and duration
VUS=500 DURATION=10m k6 run rate-limiter-test.js

# Test with different algorithm
ALGORITHM=sliding-window-log k6 run rate-limiter-test.js

# Test distributed implementation
USE_REDIS=true k6 run rate-limiter-test.js

# Custom base URL (for remote testing)
BASE_URL=https://your-api.com k6 run rate-limiter-test.js
```


### Load Test Metrics


Each test generates:
- **Console output**: Real-time progress and summary statistics
- **summary.json**: Detailed metrics for further analysis
- **Prometheus metrics**: Available at http://localhost:9090 during tests


Key metrics to monitor:
- **Requests per second (RPS)**: Throughput capacity
- **P50/P95/P99 latency**: Response time percentiles
- **Error rate**: Failed requests (should be < 5% for production)
- **Rate limit hit rate**: Percentage of requests blocked by rate limiter


### Performance Benchmarks


Simple benchmark scripts for comparing in-memory vs Redis rate limiter performance.

#### Purpose

These benchmarks are designed to showcases:
- Performance difference between in-memory and distributed implementations
- Trade-offs between latency and scalability
- Real-world load testing capabilities

#### Quick Start

**Run Individual Benchmarks:**

In-Memory Benchmark:
```bash
cd k6
k6 run benchmark-in-memory.js
```

Redis Benchmark:
```bash
cd k6
k6 run benchmark-redis.js
```

**Run Comparison (Recommended for Showcase):**

Run both benchmarks sequentially with automatic comparison:
```bash
cd k6
chmod +x run-benchmark-comparison.sh
./run-benchmark-comparison.sh
```

#### Customization

You can customize the benchmarks with environment variables:

```bash
# Custom base URL
BASE_URL=http://localhost:8080 k6 run benchmark-in-memory.js

# Different algorithm
ALGORITHM=sliding-window-log k6 run benchmark-redis.js
```

#### Expected Results

On a typical development machine (1000 req/s, 30s test):

| Metric | In-Memory | Redis |
|--------|-----------|-------|
| P50 Latency | ~0.5ms | ~0.7ms |
| P95 Latency | ~1.4ms | ~1.2ms |
| P99 Latency | ~4.7ms | ~6.5ms |
| Throughput | ~1000 req/s | ~1000 req/s |
| Success Rate | 100% | 100% |

**Actual Sample Results:**
- In-Memory: P50=0.48ms, P95=1.42ms, P99=4.67ms, 1000.03 req/s
- Redis: P50=0.67ms, P95=1.21ms, P99=6.46ms, 1000.03 req/s

**Note:** Actual performance depends on hardware, network conditions, and Redis configuration.

#### Key Talking Points

**In-Memory Implementation:**
- ✅ Lower latency (no network round-trip)
- ✅ Simpler architecture
- ❌ Limited to single instance
- ❌ State lost on restart

**Redis Implementation:**
- ✅ Distributed across instances
- ✅ Persistent state
- ✅ Horizontal scalability
- ❌ Higher latency (network overhead)
- ❌ Additional infrastructure dependency

**Trade-off Decision:**
- Use in-memory for single-instance deployments with strict latency requirements
- Use Redis for multi-instance deployments requiring horizontal scalability


### Monitoring During Load Tests


While running load tests, monitor:
- **Grafana Dashboard**: http://localhost:3000 (login: `admin` / `admin`)
- **Prometheus**: http://localhost:9090
- **Application Logs**: `docker-compose logs -f rate-limiter-service`
- **Redis Stats**: `docker exec rate-limiter-redis redis-cli info`


### Load Test Best Practices


1. **Warm-up**: Always start with the 1K UV test before scaling to 5K or 10K
2. **Configuration**: Configure rate limits before testing using the config endpoints
3. **Monitoring**: Keep Grafana and Prometheus open during tests
4. **Resource Limits**: Ensure your machine has sufficient RAM (4GB+ for 10K UV tests)
5. **Clean State**: Reset Redis between tests: `docker-compose restart redis`


---


## Useful Docker Commands


```bash
docker-compose up --build            # Start everything
docker-compose up -d --build         # Start in background
docker-compose logs -f rate-limiter-service  # View service logs
docker-compose down                  # Stop everything
docker-compose down -v               # Stop + delete volumes (full reset)
docker-compose ps                    # Check running containers
docker-compose restart rate-limiter-service  # Restart a single service
```


---


## RAM Usage


| Service | Memory      |
|---|-------------|
| Spring Boot Application | ~1.11GB     |
| Redis | ~50MB       |
| Prometheus | ~150MB      |
| Grafana | ~150MB      |
| **Total** | **~1.49GB** |


Runs comfortably on any machine with 2GB+ RAM.


---


## Cost


**$0.** Every component is open source and runs locally.

| Component | License | Cost |
|---|---|---|
| Docker Desktop | Free (personal use) | $0 |
| Java 17 | OpenJDK (GPLv2) | $0 |
| Spring Boot | Apache 2.0 | $0 |
| Redis | BSD | $0 |
| Prometheus | Apache 2.0 | $0 |
| Grafana | AGPL | $0 |


---


## License


MIT