-- Token Bucket Rate Limiter (atomic, Redis Lua script)
-- KEYS[1] = hash key storing { tokens, ts }
-- ARGV[1] = capacity      (bucket size / limit)
-- ARGV[2] = refill_rate   (tokens per second = capacity / window)
-- ARGV[3] = now           (optional epoch-seconds override for testing;
--                           pass '' or omit to use the server's real clock)
-- ARGV[4] = ttl           (seconds to keep the key alive; should be long
--                           enough for the bucket to fully refill)
--
-- Returns 1 if the request is allowed (and consumes a token), else 0.
-- Refill and consume happen inside this single script, so two concurrent
-- requests can never both read "1 token available" and both consume it.

local capacity    = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local ttl         = tonumber(ARGV[4])

if capacity == nil or capacity <= 0 then
    return redis.error_reply("capacity must be a positive number")
end
if refill_rate == nil or refill_rate <= 0 then
    return redis.error_reply("refill_rate must be a positive number")
end
if ttl == nil or ttl <= 0 then
    return redis.error_reply("ttl must be a positive number")
end

local now
if ARGV[3] ~= nil and ARGV[3] ~= '' then
    now = tonumber(ARGV[3])
else
    local t = redis.call('TIME')
    now = tonumber(t[1]) + tonumber(t[2]) / 1e6
end

local data   = redis.call('HMGET', KEYS[1], 'tokens', 'ts')
local tokens = tonumber(data[1])
local ts     = tonumber(data[2])

if tokens == nil then
    tokens = capacity
end
if ts == nil then
    ts = now
end

-- Guard against the clock appearing to go backwards (failover, clock skew)
-- so we never refill on a negative delta.
local delta = math.max(0, now - ts)
tokens = math.min(capacity, tokens + delta * refill_rate)

local allowed = 0
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

redis.call('HSET', KEYS[1], 'tokens', tokens, 'ts', now)
redis.call('EXPIRE', KEYS[1], ttl)

return allowed




-- -- Token Bucket
-- -- KEYS[1] = hash key storing { tokens, ts }
-- -- ARGV[1] = capacity (bucket size / limit)
-- -- ARGV[2] = refill_rate (tokens per second = capacity / window)
-- -- ARGV[3] = now (epoch seconds, float)
-- -- ARGV[4] = ttl to set on the key (seconds)
-- --
-- -- Refilling the bucket and consuming a token both happen inside this single
-- -- script, so two concurrent requests can never both read "1 token available"
-- -- and both successfully consume the same token.
--
--
--
-- local capacity = tonumber(ARGV[1])
-- local refill_rate = tonumber(ARGV[2])
-- local t = redis.call("TIME")
-- local now = tonumber(t[1]) + tonumber(t[2]) / 1000000
-- -- local now = tonumber(ARGV[3])
-- local ttl = tonumber(ARGV[4])
--
-- local data = redis.call('HMGET', KEYS[1], 'tokens', 'ts')
-- local tokens = tonumber(data[1])
-- local ts = tonumber(data[2])
--
-- -- if now == nil then
-- --     return redis.error_reply(
-- --         "ARGV[3] missing. ARGV1=" .. tostring(ARGV[1]) ..
-- --         " ARGV2=" .. tostring(ARGV[2]) ..
-- --         " ARGV3=" .. tostring(ARGV[3]) ..
-- --         " ARGV4=" .. tostring(ARGV[4])
-- --     )
-- -- end
--
-- if tokens == nil then
--     tokens = capacity
-- end
--
-- if ts == nil then
--     ts = now
-- end
--
-- local delta = math.max(0, now - ts)
-- tokens = math.min(capacity, tokens + delta * refill_rate)
--
-- if tokens >= 1 then
--     tokens = tokens - 1
--     redis.call('HMSET', KEYS[1], 'tokens', tokens, 'ts', now)
--     redis.call('EXPIRE', KEYS[1], ttl)
--     return 1
-- end
--
-- redis.call('HMSET', KEYS[1], 'tokens', tokens, 'ts', now)
-- redis.call('EXPIRE', KEYS[1], ttl)
-- return 0
