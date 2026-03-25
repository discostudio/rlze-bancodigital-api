package rlze.bancodigitalapi.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import rlze.bancodigitalapi.domain.exception.EntityNotFoundException;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.TransferenciaRequest;
import rlze.bancodigitalapi.application.ports.in.TransferenciaUseCase;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.event.CreditoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.DebitoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.TransferenciaRealizadaEvent;
import rlze.bancodigitalapi.domain.exception.BusinessException;
import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferenciaService implements TransferenciaUseCase {

    private final ContaRepositoryPort contaRepositoryPort;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional // Ponto vital: Garante a atomicidade da operação
    public void executarTransferencia(TransferenciaRequest request) {
        // 1. Recupera as contas (Domain objects)
        Conta origem = contaRepositoryPort.buscarPorId(request.idContaOrigem())
                .orElseThrow(() -> new EntityNotFoundException("Conta de origem não encontrada."));

        // Verifica se é a mesma conta
        if (origem.getId().equals(request.idContaDestino())) {
            throw new BusinessException("A conta de origem não pode ser igual à conta de destino.");
        }

        Conta destino = contaRepositoryPort.buscarPorId(request.idContaDestino())
                .orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada."));

        // 2. Executa a regra de negócio no Domínio
        origem.debitar(request.valor());
        destino.creditar(request.valor());
        log.info("TransferenciaService: debitar(),creditar()");

        // 3. Persiste os novos estados
        // O JPA usará o @Version para garantir que ninguém alterou essas contas
        // entre o momento que lemos e o momento que salvamos (Optimistic Lock).
        contaRepositoryPort.salvar(origem);
        contaRepositoryPort.salvar(destino);
        log.info("TransferenciaService: salvar()");


        // Dispara as notificações (publica eventos para serem consumidos)
        gerarEventos(origem.getId(), destino.getId(), request.valor(), origem.getSaldo(),  destino.getSaldo());
    }

    private void gerarEventos(String contaOrigemId, String contaDestinoId, BigDecimal valor, BigDecimal origemSaldo, BigDecimal destinoSaldo) {
        log.info("TransferenciaService: gerarEventos()");

        // Evento transferência realizada
        eventPublisher.publishEvent(new TransferenciaRealizadaEvent(
                contaOrigemId,
                contaDestinoId,
                valor
        ));

        // Evento Débito realizado
        eventPublisher.publishEvent(new DebitoRealizadoEvent(
                contaOrigemId,
                valor,
                origemSaldo));

        // Evento crédito realizado
        eventPublisher.publishEvent(new CreditoRealizadoEvent(
                contaDestinoId,
                valor,
                destinoSaldo));
    }
}
