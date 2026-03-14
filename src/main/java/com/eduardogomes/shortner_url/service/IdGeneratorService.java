package com.eduardogomes.shortner_url.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdGeneratorService {
    private static final String COUNTER_KEY = "url:id:counter";
    private static final long RANGE_SIZE = 1000L;
    private static final long INITIAL_VALUE = 14_000_000L;

    private final StringRedisTemplate redisTemplate;

    private long currentId = 0;
    private long maxId = 0;

    public IdGeneratorService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisTemplate.opsForValue()
            .setIfAbsent(COUNTER_KEY, String.valueOf(INITIAL_VALUE));
    }

    public synchronized long nextId() {
        if (currentId >= maxId) {
            fetchNewRange();
        }
        return currentId++;
    }

    private void fetchNewRange() {
        long end = redisTemplate.opsForValue()
            .increment(COUNTER_KEY, RANGE_SIZE);
        this.currentId = end - RANGE_SIZE;
        this.maxId = end;
    }
}
