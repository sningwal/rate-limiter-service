-- Sliding Window Log
-- KEYS[1] = zset key holding one entry per recent request
-- ARGV[1] = limit
-- ARGV[2] = window (seconds)
-- ARGV[3] = now, as a float (epoch seconds with sub-second precision)
-- ARGV[4] = unique member id for this request (e.g. now + a random suffix)
--
-- The trim, cardinality check and conditional add all happen inside one
-- script execution, so two concurrent callers can never both observe
-- "count < limit" and both be admitted once the true count is already at
-- the limit.

local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local member = ARGV[4]

redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - window)

local count = redis.call('ZCARD', KEYS[1])

if count < limit then
    redis.call('ZADD', KEYS[1], now, member)
    redis.call('EXPIRE', KEYS[1], window + 1)
    return 1
end

redis.call('EXPIRE', KEYS[1], window + 1)
return 0
