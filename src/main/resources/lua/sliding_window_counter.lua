-- Sliding Window Counter (weighted average of current + previous fixed window)
-- KEYS[1] = current window counter key
-- KEYS[2] = previous window counter key
-- ARGV[1] = limit
-- ARGV[2] = window (seconds)
-- ARGV[3] = now (epoch seconds, float)
--
-- The GET of both counters, the weighted-average computation, and the
-- conditional INCR happen inside a single script execution. This is exactly
-- the read -> compute -> write sequence that races under plain GET/SET,
-- made atomic by running it all on the Redis server in one EVAL.

local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local curr = tonumber(redis.call('GET', KEYS[1]) or '0')
local prev = tonumber(redis.call('GET', KEYS[2]) or '0')

local time_into_window = now % window
local weight = 1 - (time_into_window / window)
local weighted_count = curr + (prev * weight)

if weighted_count >= limit then
    return 0
end

redis.call('INCR', KEYS[1])
redis.call('EXPIRE', KEYS[1], window * 2)
return 1
