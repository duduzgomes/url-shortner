package com.eduardogomes.shortner_url.controllers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.eduardogomes.shortner_url.controllers.dto.ErrorResponse;
import com.eduardogomes.shortner_url.controllers.dto.ShortenResponse;
import com.eduardogomes.shortner_url.models.ShortenRequest;
import com.eduardogomes.shortner_url.service.UrlService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@Tag(name = "URL Shortener", description = "Endpoints para encurtar e redirecionar URLs")
public class UrlController {
    private final UrlService urlService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(
        summary = "Encurta uma URL",
        description = "Recebe uma URL longa e retorna uma URL encurtada"
    )
    @ApiResponse(
        responseCode = "201",
        description = "URL encurtada com sucesso",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ShortenResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "URL inválida ou vazia",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "429",
        description = "Limite de requisições excedido",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @PostMapping("api/v1/shorten")
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "shorten", fallbackMethod = "shortenFallback")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        var shortCode = urlService.shorten(request.longUrl());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ShortenResponse(baseUrl + "/" + shortCode));
    }

    @Operation(
        summary = "Redireciona para a URL original",
        description = "Recebe um short code e redireciona para a URL original"
    )
   
    @ApiResponse(
        responseCode = "302",
        description = "Redirecionamento para a URL original"
    )
    @ApiResponse(
        responseCode = "404",
        description = "ShortCode não encontrado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "429",
        description = "Limite de requisições excedido",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @GetMapping("/{shortCode}")
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "redirect", fallbackMethod = "redirectFallback")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) throws Exception{
        String longUrl = urlService.resolve(shortCode);

        return ResponseEntity
        .status(HttpStatus.FOUND)
        .location(URI.create(longUrl)).build();
    }

    public ResponseEntity<ErrorResponse> shortenFallback(ShortenRequest request,RequestNotPermitted ex) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ErrorResponse(429, "Rate limit excedido", "Limite de requisições excedido."));
    }

    public ResponseEntity<ErrorResponse> redirectFallback(String shortCode,RequestNotPermitted ex) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ErrorResponse(429, "Rate limit excedido", "Limite de requisições excedido."));
    }
}
