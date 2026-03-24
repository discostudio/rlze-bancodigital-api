package rlze.bancodigitalapi.infrastructure.adapters.in.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.ContaResponse;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.NovaContaRequest;
import rlze.bancodigitalapi.application.ports.in.GestaoContaUseCase;

import java.security.InvalidParameterException;
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
            @RequestParam(required = false, defaultValue = "", value = "nomeTitular")
            String nomeTitular,
            // Injeta request bruto para validar queryParams
            HttpServletRequest request) {

        // Validação de parâmetros: Se houver mais de 1 parâmetro ou o parâmetro não for 'nomeTitular'
        if (!request.getParameterMap().isEmpty() && !request.getParameterMap().containsKey("nomeTitular")
                || request.getParameterMap().size() > 1) {

            throw new InvalidParameterException("Parâmetros de busca inválidos. Use apenas 'nomeTitular'");
        }

        // Se o nome não for enviado, ele busca por "" (string vazia), trazendo todos.
        var contas = gestaoContaUseCase.listarPorNome(nomeTitular);

        return ResponseEntity.ok(contas.stream()
                .map(ContaResponse::fromDomain)
                .toList());
    }
}
