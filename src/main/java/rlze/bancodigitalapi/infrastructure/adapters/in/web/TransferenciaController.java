package rlze.bancodigitalapi.infrastructure.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.ErrorResponse;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.TransferenciaRequest;
import rlze.bancodigitalapi.application.ports.in.TransferenciaUseCase;

@Tag(name = "Transferências", description = "Endpoints para transferência entre contas bancárias")
@RestController
@RequestMapping("/v1/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaUseCase transferenciaUseCase;

    @Operation(summary = "Transfere valores entre contas", description = "Realiza o débito na origem e crédito no destino.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transferência realizada"),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente ou regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                         {
                                             "code": "BUSINESS_ERROR",
                                             "message": "Saldo insuficiente na conta: 506dc261-2730-11f1-9746-8e822b232c7c",
                                             "timestamp": "2026-03-24T11:53:09.9069273"
                                         }
                                        """
                            )
                    )),
            @ApiResponse(responseCode = "409", description = "Conflito de concorrência (tente novamente)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                         {
                                              "code": "CONCURRENCY_ERROR",
                                              "message": "A conta foi atualizada por outra operação. Tente novamente.",
                                              "timestamp": "2026-03-24T11:51:46.1626948"
                                          }
                                        """
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Uma das contas não foi encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                         {
                                             "code": "NOT_FOUND",
                                             "message": "Conta de origem não encontrada.",
                                             "timestamp": "2026-03-24T11:54:21.0765089"
                                         }
                                        """
                            )
                    ))
    })
    @PostMapping
    public ResponseEntity<Void> transferir(@RequestBody TransferenciaRequest request) {
        transferenciaUseCase.executarTransferencia(request);
        return ResponseEntity.ok().build();
    }
}
