package rlze.bancodigitalapi.application.ports.in;

import rlze.bancodigitalapi.application.dto.TransferenciaRequest;

public interface TransferenciaUseCase {

    void executarTransferencia(TransferenciaRequest request);
}
