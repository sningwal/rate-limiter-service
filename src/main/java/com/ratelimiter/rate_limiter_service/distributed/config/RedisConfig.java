package com.ratelimiter.rate_limiter_service.distributed.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, AlgorithmConfig> redisTemplate(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, AlgorithmConfig> template = new RedisTemplate<>();

        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        JacksonJsonRedisSerializer<AlgorithmConfig> jsonSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        AlgorithmConfig.class
                );

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

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