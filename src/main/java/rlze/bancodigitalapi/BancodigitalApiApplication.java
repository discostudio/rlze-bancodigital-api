package rlze.bancodigitalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // Usado para notificações - para que o Spring consiga rodar tarefas em background
@SpringBootApplication
public class BancodigitalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancodigitalApiApplication.class, args);
    }

}
