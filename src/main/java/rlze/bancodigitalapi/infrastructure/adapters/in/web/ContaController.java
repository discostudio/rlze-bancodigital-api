package rlze.bancodigitalapi.infrastructure.adapters.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rlze.bancodigitalapi.application.dto.ContaResponse;
import rlze.bancodigitalapi.application.dto.NovaContaRequest;
import rlze.bancodigitalapi.application.ports.in.GestaoContaUseCase;

import java.util.List;

@RestController
@RequestMapping("/v1/contas")
@RequiredArgsConstructor
public class ContaController {

    private final GestaoContaUseCase gestaoContaUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criar(@RequestBody NovaContaRequest request) {
        gestaoContaUseCase.criarConta(request.nomeTitular(), request.saldo());
    }

    @GetMapping
    public ResponseEntity<List<ContaResponse>> buscar(
            @RequestParam(required = false, defaultValue = "") String nome) {

        // Se o nome não for enviado, ele busca por "" (string vazia), trazendo todos.
        var contas = gestaoContaUseCase.listarPorNome(nome);

        return ResponseEntity.ok(contas.stream()
                .map(ContaResponse::fromDomain)
                .toList());
    }
}
