package com.eduardogomes.shortner_url.models;

import org.hibernate.validator.constraints.URL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ShortenRequest(
    @Schema(
        description = "URL a ser encurtada",
        example = "https://www.google.com"
    )
    @NotBlank(message = "URL não pode ser vazia")
    @URL(message = "URL inválida, verifique o formato. Exemplo: https://www.google.com")
    String longUrl) {
}
