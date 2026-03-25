package rlze.bancodigitalapi.infrastructure.adapters.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import rlze.bancodigitalapi.domain.exception.BusinessException;
import rlze.bancodigitalapi.domain.exception.EntityNotFoundException;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.ErrorResponse;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Não Encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    // 422 - Erro de Negócio (Saldo, limite, etc)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.error(ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("BUSINESS_ERROR", ex.getMessage()));
    }

    // 409 - Conflito (Optimistic Locking)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ObjectOptimisticLockingFailureException ex) {
        log.error(ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONCURRENCY_ERROR", "A conta foi atualizada por outra operação. Tente novamente."));
    }

    // 400 - Bad Request - campos inválidos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage(), ex);

        String message = "Erro na leitura do JSON.";
        Map<String, String> details = new HashMap<>();

        Throwable rootCause = ex.getRootCause();

        // 1. Se o erro for um campo com tipo errado (ex: enviar String onde era pra ser Double)
        if (rootCause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            String fieldName = ife.getPath().stream()
                    .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                    .collect(java.util.stream.Collectors.joining("."));

            message = "Valor inválido para o campo: " + fieldName;
            details.put(fieldName, "O valor '" + ife.getValue() + "' não é compatível com o tipo esperado (" + ife.getTargetType().getSimpleName() + ")");
        }
        else if (rootCause instanceof com.fasterxml.jackson.core.JsonParseException jpe) {
            message = "Parâmetro inválido. Revise os tipos informados.";
        }
        // 2. Se o erro for propriedade desconhecida ('fail-on-unknown-properties: true' configurado no application.yml)
        else if (rootCause instanceof com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException upe) {
            String fieldName = upe.getPropertyName();
            message = "Propriedade não reconhecida.";
            details.put(fieldName, "Este campo não existe no contrato da API.");
        }
        // 3. Se for erro de sintaxe bruta (ex: faltou uma vírgula ou aspas)
        else {
            message = "O payload enviado contém erro de sintaxe ou formato inválido.";
        }

        ErrorResponse errorBody = new ErrorResponse(
                "INVALID_JSON",
                message,
                LocalDateTime.now(),
                details.isEmpty() ? null : details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(InvalidParameterException ex) {
        log.error(ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PARAMETER", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PARAMETER", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);

        // 1. Map Nome do Campo -> Mensagem de Erro
        Map<String, String> detalhesDosErros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido",
                        (existente, novo) -> existente + " e " + novo // Caso o mesmo campo tenha 2 erros (ex: @NotNull e @Positive)
                ));

        // 2. ErrorResponse usando o seu construtor completo
        ErrorResponse errorBody = new ErrorResponse(
                "INVALID_PARAMETER",
                "Um ou mais campos possuem erros de validação.",
                LocalDateTime.now(),
                detalhesDosErros
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }
}
