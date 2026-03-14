package com.eduardogomes.shortner_url.service;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static com.eduardogomes.shortner_url.common.UrlConstants.CACHE_KEY;
import static com.eduardogomes.shortner_url.common.UrlConstants.VALID_ID;
import static com.eduardogomes.shortner_url.common.UrlConstants.VALID_LONG_URL;
import static com.eduardogomes.shortner_url.common.UrlConstants.VALID_SHORT_CODE;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.eduardogomes.shortner_url.models.Url;
import com.eduardogomes.shortner_url.models.exceptions.UrlNotFoundException;
import com.eduardogomes.shortner_url.repositories.UrlRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @InjectMocks
    private UrlService urlService;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private IdGeneratorService idGeneratorService;

    @Mock
    private Base62EncoderService base62EncoderService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void shorten_WithValidUrl_ReturnsShortCode() {
        when(idGeneratorService.nextId()).thenReturn(VALID_ID);
        when(base62EncoderService.encode(VALID_ID)).thenReturn(VALID_SHORT_CODE);

        String sut = urlService.shorten(VALID_LONG_URL);
        
        assertThat(sut).isEqualTo(VALID_SHORT_CODE);
        verify(urlRepository).save(any(Url.class));
    }


    @Test
    void resolve_WithCachedShortCode_ReturnsLongUrlFromCache() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(VALID_LONG_URL);

        String longUrl = urlService.resolve(VALID_SHORT_CODE);

        assertThat(longUrl).isEqualTo(VALID_LONG_URL);
        verify(urlRepository, never()).findById(anyString());
    }

    @Test
    void resolve_WithUncachedShortCode_ReturnsLongUrlFromRepository() throws Exception {
        Url url = Url.builder()
            .shortCode(VALID_SHORT_CODE)
            .longUrl(VALID_LONG_URL)
            .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        when(urlRepository.findById(VALID_SHORT_CODE)).thenReturn(Optional.of(url));

        String longUrl = urlService.resolve(VALID_SHORT_CODE);

        assertThat(longUrl).isEqualTo(VALID_LONG_URL);
        verify(valueOperations).set(anyString(), anyString(), any());
    }

    @Test
    void resolve_WithInvalidShortCode_ThrowsUrlNotFoundException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        when(urlRepository.findById(VALID_SHORT_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.resolve(VALID_SHORT_CODE))
            .isInstanceOf(UrlNotFoundException.class);
    }
}
