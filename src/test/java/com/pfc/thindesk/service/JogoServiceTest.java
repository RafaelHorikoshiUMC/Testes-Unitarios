package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.repository.JogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class JogoServiceTest {

    @InjectMocks
    private JogoService jogoService;

    @Mock
    private JogoRepository jogoRepository;

    private Jogo jogo;

    @BeforeEach
    void setUp() {
        jogo = new Jogo();
        jogo.setId("jogo1");
        jogo.setNome("Jogo de Teste");
    }

    @Test
    void testCriarJogo() {
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(i -> i.getArgument(0));

        Jogo novoJogo = new Jogo();
        novoJogo.setNome("Novo Jogo");

        Jogo criado = jogoService.criarJogo(novoJogo);

        assertNotNull(criado);
        assertEquals("Novo Jogo", criado.getNome());
    }

    @Test
    void testListarTodosJogos() {
        when(jogoRepository.findAll()).thenReturn(List.of(jogo, new Jogo()));

        List<Jogo> jogos = jogoService.listarTodosJogos();

        assertNotNull(jogos);
        assertEquals(2, jogos.size());
    }

    @Test
    void testBuscarJogoPorId_Encontrado() {
        when(jogoRepository.findById("jogo1")).thenReturn(Optional.of(jogo));

        Optional<Jogo> result = jogoService.buscarJogoPorId("jogo1");

        assertTrue(result.isPresent());
        assertEquals("jogo1", result.get().getId());
    }

    @Test
    void testBuscarJogoPorId_NaoEncontrado() {
        when(jogoRepository.findById("jogo2")).thenReturn(Optional.empty());

        Optional<Jogo> result = jogoService.buscarJogoPorId("jogo2");

        assertFalse(result.isPresent());
    }

    @Test
    void testAtualizarJogo_Sucesso() {
        Jogo jogoAtualizado = new Jogo();
        jogoAtualizado.setNome("Jogo Atualizado");

        when(jogoRepository.existsById("jogo1")).thenReturn(true);
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(i -> i.getArgument(0));

        Jogo atualizado = jogoService.atualizarJogo("jogo1", jogoAtualizado);

        assertNotNull(atualizado);
        assertEquals("Jogo Atualizado", atualizado.getNome());
    }

    @Test
    void testAtualizarJogo_NaoEncontrado() {
        Jogo jogoAtualizado = new Jogo();
        jogoAtualizado.setNome("Jogo Atualizado");

        when(jogoRepository.existsById("jogo2")).thenReturn(false);

        Jogo resultado = jogoService.atualizarJogo("jogo2", jogoAtualizado);

        assertNull(resultado);
    }

    @Test
    void testDeletarJogo() {
        when(jogoRepository.existsById("jogo1")).thenReturn(true); // << importante!

        doNothing().when(jogoRepository).deleteById("jogo1");

        assertTrue(jogoService.deletarJogo("jogo1")); // Também pode verificar o retorno

        verify(jogoRepository).deleteById("jogo1");
    }

}
