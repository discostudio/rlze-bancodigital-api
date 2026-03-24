package rlze.bancodigitalapi.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import rlze.bancodigitalapi.infrastructure.adapters.in.web.dto.TransferenciaRequest;
import rlze.bancodigitalapi.application.ports.in.TransferenciaUseCase;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.event.CreditoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.DebitoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.TransferenciaRealizadaEvent;
import rlze.bancodigitalapi.domain.exception.BusinessException;
import rlze.bancodigitalapi.domain.model.Conta;

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
                .orElseThrow(() -> new BusinessException("Conta de origem não encontrada."));

        Conta destino = contaRepositoryPort.buscarPorId(request.idContaDestino())
                .orElseThrow(() -> new BusinessException("Conta de destino não encontrada."));

        // 2. Executa a regra de negócio no Domínio
        origem.debitar(request.valor());
        destino.creditar(request.valor());

        // 3. Persiste os novos estados
        // O JPA usará o @Version para garantir que ninguém alterou essas contas
        // entre o momento que lemos e o momento que salvamos (Optimistic Lock).
        contaRepositoryPort.salvar(origem);
        contaRepositoryPort.salvar(destino);

        System.out.println("Transferência de R$ " + request.valor() + " concluída com sucesso!");

        // Dispara as notificações (publica eventos para serem consumidos)
        eventPublisher.publishEvent(new TransferenciaRealizadaEvent(
                origem.getId(),
                destino.getId(),
                request.valor()
        ));
        eventPublisher.publishEvent(new DebitoRealizadoEvent(
                origem.getId(),
                request.valor(),
                origem.getSaldo()));
        eventPublisher.publishEvent(new CreditoRealizadoEvent(
                destino.getId(),
                request.valor(),
                destino.getSaldo()));
    }
}
