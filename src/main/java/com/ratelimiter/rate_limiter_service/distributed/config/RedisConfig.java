package com.ratelimiter.rate_limiter_service.distributed.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis client configuration.
 * Every rate-limiting algorithm is executed as a single Lua script via EVALSHA/EVAL,
 * so the read-modify-write cycle for each algorithm is atomic from Redis's point of
 * view, even under heavy concurrent load from many application instances.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultRedisScript<Long> fixedWindowScript() {
        return script("lua/fixed_window.lua");
    }

    @Bean
    public DefaultRedisScript<Long> slidingWindowLogScript() {
        return script("lua/sliding_window_log.lua");
    }

    @Bean
    public DefaultRedisScript<Long> slidingWindowCounterScript() {
        return script("lua/sliding_window_counter.lua");
    }

    @Bean
    public DefaultRedisScript<Long> tokenBucketScript() {
        return script("lua/token_bucket.lua");
    }

    @Bean
    public DefaultRedisScript<Long> leakyBucketScript() {
        return script("lua/leaky_bucket.lua");
    }

    private DefaultRedisScript<Long> script(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }

}
