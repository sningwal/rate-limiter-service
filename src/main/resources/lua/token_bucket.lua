-- Token Bucket
-- KEYS[1] = hash key storing { tokens, ts }
-- ARGV[1] = capacity (bucket size / limit)
-- ARGV[2] = refill_rate (tokens per second = capacity / window)
-- ARGV[3] = now (epoch seconds, float)
-- ARGV[4] = ttl to set on the key (seconds)
--
-- Refilling the bucket and consuming a token both happen inside this single
-- script, so two concurrent requests can never both read "1 token available"
-- and both successfully consume the same token.

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local ttl = tonumber(ARGV[4])

local data = redis.call('HMGET', KEYS[1], 'tokens', 'ts')
local tokens = tonumber(data[1])
local ts = tonumber(data[2])

if tokens == nil then
    tokens = capacity
    ts = now
end

local delta = math.max(0, now - ts)
tokens = math.min(capacity, tokens + delta * refill_rate)

if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'ts', now)
    redis.call('EXPIRE', KEYS[1], ttl)
    return 1
end

redis.call('HMSET', KEYS[1], 'tokens', tokens, 'ts', now)
redis.call('EXPIRE', KEYS[1], ttl)
return 0
