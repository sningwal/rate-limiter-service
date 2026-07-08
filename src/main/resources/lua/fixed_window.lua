-- Fixed Window Counter
-- KEYS[1] = counter key, already suffixed with the current window index by the caller
-- ARGV[1] = limit
-- ARGV[2] = window (seconds), used as the TTL
--
-- INCR + EXPIRE both run inside this single script invocation, which Redis
-- executes atomically with respect to all other clients/scripts. This closes
-- the race that a separate GET-then-INCR-then-EXPIRE would have.

local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

local count = redis.call('INCR', KEYS[1])
if count == 1 then
    redis.call('EXPIRE', KEYS[1], window)
end

if count > limit then
    return 0
end
return 1
