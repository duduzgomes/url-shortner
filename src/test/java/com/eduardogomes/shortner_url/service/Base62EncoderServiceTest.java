package com.eduardogomes.shortner_url.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class Base62EncoderServiceTest {
    private static final long VALID_ID = 14_000_000L;
    private static final long ANOTHER_VALID_ID = 14_000_001L;
    private static final String INVALID_SHORT_CODE = "@@@invalido@@@";

    private Base62EncoderService encoder;

    @BeforeEach
    void setUp() {
        encoder = new Base62EncoderService();
    }

    @Test
    void encode_WithValidId_ReturnsShortCode() {
        String shortCode = encoder.encode(VALID_ID);
        assertThat(shortCode)
            .isNotNull()
            .isNotBlank();
    }

    @Test
    void decode_WithValidShortCode_ReturnsOriginalId() {
        String shortCode = encoder.encode(VALID_ID);
        long idDecodado = encoder.decode(shortCode);
        assertThat(idDecodado).isEqualTo(VALID_ID);
    }

    @Test
    void encode_WithValidId_ReturnsShortCodeWithMinimum4Characters() {
        String shortCode = encoder.encode(VALID_ID);
        assertThat(shortCode.length()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void encode_WithDifferentIds_ReturnsDifferentShortCodes() {
        String shortCode1 = encoder.encode(VALID_ID);
        String shortCode2 = encoder.encode(ANOTHER_VALID_ID);
        assertThat(shortCode1).isNotEqualTo(shortCode2);
    }

    @Test
    void encode_WithSameId_ReturnsSameShortCode() {
        String shortCode1 = encoder.encode(VALID_ID);
        String shortCode2 = encoder.encode(VALID_ID);
        assertThat(shortCode1).isEqualTo(shortCode2);
    }

    @Test
    void decode_WithInvalidShortCode_ReturnsMinusOne() {
        long resultado = encoder.decode(INVALID_SHORT_CODE);
        assertThat(resultado).isEqualTo(-1L);
    }
}
