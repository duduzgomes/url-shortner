package com.eduardogomes.shortner_url.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShortenResponse(
    @Schema(description = "URL encurtada completa", example = "http://localhost:8080/xK9p")
    String shortUrl
) {}
