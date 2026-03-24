package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) // Não mostra campos nulos no JSON
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        Map<String, String> details // Útil para erros de validação de campos
) {
    // Construtor auxiliar para erros simples
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now(), null);
    }
}
