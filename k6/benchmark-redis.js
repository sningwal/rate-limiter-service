import http from 'k6/http';
import { check } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// Simple benchmark for Redis-backed Rate Limiter
// Purpose: Showcase performance of distributed Redis implementation

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ALGORITHM = __ENV.ALGORITHM || 'token-bucket';

export const options = {
    scenarios: {
        benchmark: {
            executor: 'constant-arrival-rate',
            rate: 1000, // 1000 requests per second
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 100,
            maxVUs: 500,
        },
    },
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(50)', 'p(90)', 'p(95)', 'p(99)'],
};

export default function () {
    const clientId = `client-${__VU}-${__ITER}`;
    
    const endpoint = `${BASE_URL}/api/v1/redis/resource/${ALGORITHM}`;
    
    const response = http.get(endpoint, {
        headers: {
            'Content-Type': 'application/json',
            'X-Client-Id': clientId,
        },
    });
    
    check(response, {
        'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
    });
}

export function handleSummary(data) {
    const httpReqDuration = data.metrics.http_req_duration;
    const httpReqFailed = data.metrics.http_req_failed;
    const httpReqs = data.metrics.http_reqs;
    
    const p50 = httpReqDuration.values['p(50)'];
    const p95 = httpReqDuration.values['p(95)'];
    const p99 = httpReqDuration.values['p(99)'];
    
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: false }) + `\n
=== REDIS BENCHMARK SUMMARY ===
Requests/sec: ${(httpReqs.values.count / 30).toFixed(2)}
P50 Latency: ${p50 ? p50.toFixed(2) + 'ms' : 'N/A'}
P95 Latency: ${p95 ? p95.toFixed(2) + 'ms' : 'N/A'}
P99 Latency: ${p99 ? p99.toFixed(2) + 'ms' : 'N/A'}
Success Rate: ${((1 - httpReqFailed.values.rate) * 100).toFixed(2)}%
`,
    };
}
