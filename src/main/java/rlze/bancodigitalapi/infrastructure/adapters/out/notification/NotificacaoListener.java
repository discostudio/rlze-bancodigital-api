package rlze.bancodigitalapi.infrastructure.adapters.out.notification;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import rlze.bancodigitalapi.domain.event.CreditoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.DebitoRealizadoEvent;
import rlze.bancodigitalapi.domain.event.TransferenciaRealizadaEvent;

@Component
public class NotificacaoListener {

    // Consumo do evento
    // Comportamento do listener. Nessa classe seria a integração com AWS SNS, SendGrid, Twilio, etc.

    @Async // Faz rodar em uma thread separada
    @EventListener
    public void handleTransferencia(TransferenciaRealizadaEvent event) {
        System.out.println("---------------------------------------------------------");
        System.out.println("[NOTIFICAÇÃO TRANSFERÊNCIA]");
        System.out.println("Conta Origem: " + event.idOrigem());
        System.out.println("Conta Destino: " + event.idDestino());
        System.out.println("Valor: R$ " + event.valor());
        System.out.println("---------------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleDebito(DebitoRealizadoEvent event) {
        System.out.println("[NOTIFICAÇÃO DÉBITO] Conta: " + event.idConta() +
                " | Valor retirado: R$ " + event.valor() +
                " | Saldo Atual: R$ " + event.saldoAtualizado());
    }

    @Async
    @EventListener
    public void handleCredito(CreditoRealizadoEvent event) {
        System.out.println("[NOTIFICAÇÃO CRÉDITO] Conta: " + event.idConta() +
                " | Valor recebido: R$ " + event.valor() +
                " | Saldo Atual: R$ " + event.saldoAtualizado());
    }
}
