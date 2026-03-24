package rlze.bancodigitalapi.application.ports.in;

import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.TransferenciaRequest;

public interface TransferenciaUseCase {

    void executarTransferencia(TransferenciaRequest request);
}
