#!/bin/bash

# Benchmark Comparison Script
# Runs both in-memory and Redis benchmarks and compares results
# Purpose: Interview/hiring manager showcase

set -e

BASE_URL=${BASE_URL:-http://localhost:8080}
ALGORITHM=${ALGORITHM:-token-bucket}
DURATION=${DURATION:-30s}
RATE=${RATE:-1000}

echo "=========================================="
echo "RATE LIMITER BENCHMARK COMPARISON"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "Algorithm: $ALGORITHM"
echo "Duration: $DURATION"
echo "Rate: $RATE req/s"
echo "=========================================="
echo ""

# Check if service is running
echo "Checking if service is running..."
if ! curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "ERROR: Service is not running at $BASE_URL"
    echo "Please start the service with: docker-compose up -d"
    exit 1
fi
echo "✓ Service is running"
echo ""

# Run In-Memory Benchmark
echo "=========================================="
echo "RUNNING IN-MEMORY BENCHMARK"
echo "=========================================="
k6 run \
    --env BASE_URL="$BASE_URL" \
    --env ALGORITHM="$ALGORITHM" \
    benchmark-in-memory.js

echo ""
echo "=========================================="
echo "RUNNING REDIS BENCHMARK"
echo "=========================================="
k6 run \
    --env BASE_URL="$BASE_URL" \
    --env ALGORITHM="$ALGORITHM" \
    benchmark-redis.js

echo ""
echo "=========================================="
echo "BENCHMARK COMPARISON COMPLETE"
echo "=========================================="
echo ""
echo "Key Takeaways:"
echo "- In-Memory: Lower latency, no network overhead"
echo "- Redis: Distributed, scalable across instances"
echo "- Trade-off: Latency vs Scalability"
