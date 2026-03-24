package rlze.bancodigitalapi.infrastructure.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.ContaResponse;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.ErrorResponse;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.NovaContaRequest;
import rlze.bancodigitalapi.application.ports.in.GestaoContaUseCase;

import java.security.InvalidParameterException;
import java.util.List;

@Tag(name = "Contas", description = "Endpoints para gestão de contas bancárias")
@RestController
@RequestMapping("/v1/contas")
@RequiredArgsConstructor
public class ContaController {

    private final GestaoContaUseCase gestaoContaUseCase;

    @Operation(summary = "Cria uma nova conta bancária", description = "Executa a criação de uma nova conta bancária com base nos parâmetros informados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                         {
                                             "code": "INVALID_JSON",
                                             "message": "O payload enviado contém campos desconhecidos ou inválidos.",
                                             "timestamp": "2026-03-24T11:48:25.5478227"
                                         }
                                        """
                            )
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criar(@RequestBody NovaContaRequest request) {
        gestaoContaUseCase.criarConta(request.nomeTitular(), request.saldo());
    }

    @Operation(summary = "Busca contas por nome do titular", description = "Retorna as contas encontradas pelo nome do titular informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma conta encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                         {
                                             "code": "NOT_FOUND",
                                             "message": "Nenhuma conta encontrada para o nome: teste",
                                             "timestamp": "2026-03-24T11:51:16.0872921"
                                         }
                                        """
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Campos inválidos no request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                         {
                                              "code": "INVALID_PARAMETER",
                                              "message": "Parâmetros de busca inválidos. Use apenas 'nomeTitular'",
                                              "timestamp": "2026-03-24T11:51:46.1626948"
                                          }
                                        """
                            )
                    ))
    })
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
