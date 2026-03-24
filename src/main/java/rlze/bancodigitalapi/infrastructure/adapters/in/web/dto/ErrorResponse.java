package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "DTO para retornar info de erros")
@JsonInclude(JsonInclude.Include.NON_NULL) // Não mostra campos nulos no JSON
public record ErrorResponse(
        @Schema(description = "Código do erro", example = "NOT_FOUND")
        String code,
        @Schema(description = "Mensagem de erro", example = "A conta foi atualizada por outra operação. Tente novamente.")
        String message,
        @Schema(description = "Data do erro", example = "2026-03-24T11:40:44.4404661")
        LocalDateTime timestamp,
        @Schema(description = "Detalhes do erro", example = "Campo 'nomeTitular' não informado")
        Map<String, String> details // Útil para erros de validação de campos
) {
    // Construtor auxiliar para erros simples
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now(), null);
    }
}
