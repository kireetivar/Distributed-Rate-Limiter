-- KEYS[1] = The unique key
-- ARGV[1] = capacity
-- ARGV[2] = refill_rate
-- ARGV[3] = current_timestamp
-- ARGV[4] = requested_tokens

-- 1. FIX: Use Uppercase KEYS and ARGV
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local last_tokens = tonumber(redis.call('hget', key, 'tokens'))
local last_refill = tonumber(redis.call('hget', key, 'last_refill'))

-- 2. FIX: Handle the "First Time" case
-- If these are nil, it means this user has never visited before.
if last_tokens == nil then
    last_tokens = capacity
    last_refill = now
end

-- 3. Refill Logic
local delta = math.max(0, now - last_refill)
local tokens_to_add = delta * refill_rate
local new_tokens = math.min(capacity, last_tokens + tokens_to_add)

if new_tokens >= requested then
    new_tokens = new_tokens - requested
    redis.call('hset', key, 'tokens', new_tokens, 'last_refill', now)
    redis.call('expire', key, 3600)
    return 1 -- Allowed
else
    -- Even if blocked, update the refill time to prevent "time travel" bugs
    redis.call('hset', key, 'tokens', new_tokens, 'last_refill', now)
    return 0 -- Blocked
end