package rlze.bancodigitalapi.application.usecases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContaServiceTest {

    @Mock
    private ContaRepositoryPort contaRepositoryPort;

    @InjectMocks
    private GestaoContaService contaService;

    @Captor
    private ArgumentCaptor<Conta> contaCaptor; // Captura o objeto enviado ao repository

    @Test
    @DisplayName("Deve criar uma nova conta e chamar o repository com os dados corretos")
    void deveCriarContaComSucesso() {
        // Arrange
        String nomeTitular = "Joaquim Silveira";
        BigDecimal saldoInicial = new BigDecimal("1000.00");

        // Act
        contaService.criarConta(nomeTitular, saldoInicial);

        // Assert
        // 1. Verificamos se o salvar foi chamado uma vez
        verify(contaRepositoryPort, times(1)).salvar(contaCaptor.capture());

        // 2. Pegamos o objeto que foi "capturado" na chamada do salvar
        Conta contaPersistida = contaCaptor.getValue();

        // 3. Validamos o estado desse objeto
        assertNotNull(contaPersistida.getId(), "O Service deve ter gerado um UUID para a conta");
        assertEquals(nomeTitular, contaPersistida.getNomeTitular());
        assertEquals(saldoInicial, contaPersistida.getSaldo());

        // Se você inicializa a versão como 0 no domínio:
        assertEquals(0, contaPersistida.getVersion());
    }

    @Test
    @DisplayName("Deve buscar contas por nome com sucesso")
    void deveBuscarContasPorNome() {
        // 1. Arrange: Criamos uma lista com a conta mockada
        String nomeBusca = "Bob Silva";
        Conta contaMock = new Conta(nomeBusca, new BigDecimal("500.00"));
        List<Conta> listaMock = List.of(contaMock);

        // Configuramos o mock para retornar a LISTA quando o repository for chamado
        when(contaRepositoryPort.buscarPorNome(nomeBusca)).thenReturn(listaMock);

        // 2. Act: Chamamos o service
        List<Conta> resultado = contaService.listarPorNome(nomeBusca);

        // 3. Assert: Validações
        assertNotNull(resultado);
        assertEquals(1, resultado.size(), "A lista deve conter exatamente 1 conta");
        assertEquals(nomeBusca, resultado.get(0).getNomeTitular());

        // Verifica se o método correto do repository foi chamado com o parâmetro certo
        verify(contaRepositoryPort).buscarPorNome(nomeBusca);
    }

    /*@Test
    @DisplayName("Deve retornar uma conta quando o ID existir")
    void deveRetornarContaQuandoIdExistir() {
        // 1. Arrange (Preparação)
        String idExistente = UUID.randomUUID().toString();
        Conta contaMock = new Conta(idExistente, "Joaquim Silveira", new BigDecimal("1000.00"));

        // Configuramos o port para retornar um Optional com a conta
        when(contaRepositoryPort.buscarPorId(idExistente)).thenReturn(Optional.of(contaMock));

        // 2. Act (Execução)
        Conta resultado = contaService.buscarPorId(idExistente);

        // 3. Assert (Verificação)
        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getId());
        assertEquals("Joaquim Silveira", resultado.getNomeTitular());

        // Garante que o repositório foi chamado exatamente uma vez com o ID correto
        verify(contaRepositoryPort, times(1)).buscarPorId(idExistente);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o ID não for encontrado")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        // 1. Arrange
        String idInexistente = "id-que-nao-existe";
        when(contaRepositoryPort.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert (Execução e Verificação da Exceção)
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contaService.buscarPorId(idInexistente);
        });

        // Valida se a mensagem de erro está correta (conforme definido no seu UseCase)
        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepositoryPort, times(1)).buscarPorId(idInexistente);
    }*/
}
