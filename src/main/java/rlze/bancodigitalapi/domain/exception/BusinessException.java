package rlze.bancodigitalapi.domain.exception;

// 422 - Regra de negócio violada
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
