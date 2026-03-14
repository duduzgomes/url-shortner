package com.eduardogomes.shortner_url.models.exceptions;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortCode) {
        super("URL não encontrada para o código: " + shortCode);
    }
}
