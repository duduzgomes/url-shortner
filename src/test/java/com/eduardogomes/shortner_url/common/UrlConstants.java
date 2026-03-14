package com.eduardogomes.shortner_url.common;

import java.time.Instant;

import com.eduardogomes.shortner_url.models.Url;

public class UrlConstants {
    public static final String VALID_LONG_URL = "https://www.google.com";
    public static final String VALID_SHORT_CODE = "xK9p";
    public static final long VALID_ID = 14_000_000L;
    public static final String CACHE_KEY = "url:cache:" + VALID_SHORT_CODE;
    public static final Url URL = Url.builder()
            .shortCode(VALID_SHORT_CODE)
            .longUrl(VALID_LONG_URL)
            .createdAt(Instant.now())
            .build(); 
   
}
