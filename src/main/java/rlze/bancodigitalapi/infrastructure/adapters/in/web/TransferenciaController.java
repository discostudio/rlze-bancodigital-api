package rlze.bancodigitalapi.infrastructure.adapters.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rlze.bancodigitalapi.application.dto.TransferenciaRequest;
import rlze.bancodigitalapi.application.ports.in.TransferenciaUseCase;

@RestController
@RequestMapping("/v1/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaUseCase transferenciaUseCase;

    @PostMapping
    public ResponseEntity<Void> transferir(@RequestBody TransferenciaRequest request) {
        transferenciaUseCase.executarTransferencia(request);
        return ResponseEntity.ok().build();
    }
}
