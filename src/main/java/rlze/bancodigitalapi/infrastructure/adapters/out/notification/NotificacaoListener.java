package rlze.bancodigitalapi.infrastructure.adapters.out.notification;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import rlze.bancodigitalapi.domain.event.TransferenciaRealizadaEvent;

@Component
public class NotificacaoListener {

    @Async // Faz rodar em uma thread separada
    @EventListener
    public void handleTransferencia(TransferenciaRealizadaEvent event) {
        // Aqui você integraria com AWS SNS, SendGrid, Twilio, etc.
        System.out.println("---------------------------------------------------------");
        System.out.println("NOTIFICAÇÃO ENVIADA COM SUCESSO!");
        System.out.println("Origem: " + event.idOrigem());
        System.out.println("Valor: R$ " + event.valor());
        System.out.println("---------------------------------------------------------");
    }
}
