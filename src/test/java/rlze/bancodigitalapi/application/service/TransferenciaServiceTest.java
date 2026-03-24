package rlze.bancodigitalapi.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.TransferenciaRequest;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.event.CreditoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.DebitoRealizadoEvent;
import rlze.bancodigitalapi.domain.exception.BusinessException;
import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferenciaServiceTest {

    @Mock
    private ContaRepositoryPort repositoryPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferenciaService service;

    @Test
    @DisplayName("Deve executar transferencia entre duas contas com sucesso")
    void deveTransferirComSucesso() {
        // Arrange
        String idOrigem = "A";
        String idDestino = "B";
        BigDecimal valor = new BigDecimal("100.00");

        /*Conta origem = new Conta(idOrigem, "Origem", new BigDecimal("500.00"));
        Conta destino = new Conta(idDestino, "Destino", new BigDecimal("200.00"));*/
        Conta origem = new Conta("Origem", new BigDecimal("500.00"));
        Conta destino = new Conta("Destino", new BigDecimal("200.00"));

        when(repositoryPort.buscarPorId(idOrigem)).thenReturn(Optional.of(origem));
        when(repositoryPort.buscarPorId(idDestino)).thenReturn(Optional.of(destino));

        TransferenciaRequest request = new TransferenciaRequest(idOrigem, idDestino, valor);

        // Act
        service.executarTransferencia(request);

        // Assert
        assertEquals(new BigDecimal("400.00"), origem.getSaldo());
        assertEquals(new BigDecimal("300.00"), destino.getSaldo());

        // Verifica se salvou ambas as contas
        verify(repositoryPort, times(2)).salvar(any(Conta.class));

        // Verifica se disparou os dois eventos de notificação
        verify(eventPublisher).publishEvent(any(DebitoRealizadoEvent.class));
        verify(eventPublisher).publishEvent(any(CreditoRealizadoEvent.class));
    }

    @Test
    @DisplayName("Deve falhar se a conta de origem nao existir")
    void deveFalharContaInexistente() {
        when(repositoryPort.buscarPorId("A")).thenReturn(Optional.empty());

        TransferenciaRequest request = new TransferenciaRequest("A", "B", BigDecimal.TEN);

        assertThrows(BusinessException.class, () -> service.executarTransferencia(request));
        verify(repositoryPort, never()).salvar(any());
    }
}
