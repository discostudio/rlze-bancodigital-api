package rlze.bancodigitalapi.infrastructure.adapters.in.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import rlze.bancodigitalapi.domain.exception.BusinessException;
import rlze.bancodigitalapi.domain.exception.EntityNotFoundException;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.ErrorResponse;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Não Encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    // 422 - Erro de Negócio (Saldo, limite, etc)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("BUSINESS_ERROR", ex.getMessage()));
    }

    // 409 - Conflito (Optimistic Locking)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONCURRENCY_ERROR", "A conta foi atualizada por outra operação. Tente novamente."));
    }

    // 400 - Bad Request - campos inválidos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        // Verifica se a causa da falha no JSON foi uma IllegalArgumentException (lançada pelo Record)
        Throwable rootCause = ex.getRootCause();

        if (ex.getRootCause() instanceof IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_PARAMETER", rootCause.getMessage()));
        }

        // Se for outro erro de JSON (syntax, campos desconhecidos), mantém a mensagem genérica
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_JSON", "O payload enviado contém campos desconhecidos ou formato inválido."));
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(InvalidParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PARAMETER", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PARAMETER", ex.getMessage()));
    }
}
