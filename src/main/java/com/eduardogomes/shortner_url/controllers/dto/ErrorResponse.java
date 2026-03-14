package com.eduardogomes.shortner_url.controllers.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de erro")
public class ErrorResponse {

    @Schema(description = "Código do erro", example = "400")
    private int status;

    @Schema(description = "Tipo do erro", example = "Validação falhou")
    private String error;

    @Schema(description = "Mensagem detalhada", example = "URL não pode ser vazia")
    private String message;

    @Schema(description = "Momento do erro", example = "2026-03-13T20:00:00Z")
    private Instant timestamp;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
