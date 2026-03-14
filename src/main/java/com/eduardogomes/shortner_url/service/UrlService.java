package com.eduardogomes.shortner_url.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.eduardogomes.shortner_url.models.Url;
import com.eduardogomes.shortner_url.models.exceptions.UrlNotFoundException;
import com.eduardogomes.shortner_url.repositories.UrlRepository;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class UrlService {

    private static final String CACHE_PREFIX = "url:cache:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final UrlRepository urlRepository;
    private final IdGeneratorService idGeneratorService;
    private final Base62EncoderService base62EncoderService;
    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;


    public UrlService(UrlRepository urlRepository, IdGeneratorService idGeneratorService,
            Base62EncoderService base62EncoderService, StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
        this.urlRepository = urlRepository;
        this.idGeneratorService = idGeneratorService;
        this.base62EncoderService = base62EncoderService;
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        
    }

    public String shorten(String longUrl) {
        long id = idGeneratorService.nextId();

        String shortCode = base62EncoderService.encode(id);

        Url url = Url.builder()
            .shortCode(shortCode)
            .longUrl(longUrl)
            .createdAt(Instant.now())
            .build();

        urlRepository.save(url);

        meterRegistry.counter("url.shortened.total").increment();

        return shortCode;
    }

    public String resolve(String shortCode) throws Exception {
        String cacheKey = CACHE_PREFIX + shortCode;

        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            meterRegistry.counter("url.cache.hit").increment();
            return cached;
        }

        Optional<Url> url = urlRepository.findById(shortCode);

        meterRegistry.counter("url.cache.miss").increment();

        String longUrl = url.map(Url::getLongUrl)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));
        
        redisTemplate.opsForValue().set(cacheKey, longUrl, CACHE_TTL);

        return longUrl;
    }
}
