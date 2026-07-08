-- Leaky Bucket, implemented as a "water level" rather than a literal queue.
-- KEYS[1] = hash key storing { level, ts }
-- ARGV[1] = capacity (limit)
-- ARGV[2] = leak_rate (requests drained per second = capacity / window)
-- ARGV[3] = now (epoch seconds, float)
-- ARGV[4] = ttl to set on the key (seconds)
--
-- Draining the bucket by elapsed time and adding one unit both happen inside
-- this single script, avoiding the LLEN-then-LPUSH race of a naive
-- list-based implementation.

local capacity = tonumber(ARGV[1])
local leak_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local ttl = tonumber(ARGV[4])

local data = redis.call('HMGET', KEYS[1], 'level', 'ts')
local level = tonumber(data[1])
local ts = tonumber(data[2])

if level == nil then
    level = 0
    ts = now
end

local elapsed = math.max(0, now - ts)
level = math.max(0, level - (elapsed * leak_rate))

if level < capacity then
    level = level + 1
    redis.call('HMSET', KEYS[1], 'level', level, 'ts', now)
    redis.call('EXPIRE', KEYS[1], ttl)
    return 1
end

redis.call('HMSET', KEYS[1], 'level', level, 'ts', now)
redis.call('EXPIRE', KEYS[1], ttl)
return 0
