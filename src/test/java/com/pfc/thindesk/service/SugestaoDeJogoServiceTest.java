package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.SugestaoDeJogo;
import com.pfc.thindesk.repository.SugestaoDeJogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SugestaoDeJogoServiceTest {

    @InjectMocks
    private SugestaoDeJogoService sugestaoDeJogoService;

    @Mock
    private SugestaoDeJogoRepository sugestaoDeJogoRepository;

    @Mock
    private PerfilService perfilService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void criarSugestaoDeJogo_sucesso() {
        Perfil perfil = new Perfil();
        perfil.setId("123");
        perfil.setApelido("Player1");

        SugestaoDeJogo sugestao = new SugestaoDeJogo();
        sugestao.setNomeDoJogoSugerido("Jogo X");

        when(authentication.getName()).thenReturn("user@email.com");
        when(perfilService.buscarPerfilDoUsuarioLogado("user@email.com"))
                .thenReturn(Optional.of(perfil));
        when(sugestaoDeJogoRepository.save(any(SugestaoDeJogo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SugestaoDeJogo resultado = sugestaoDeJogoService.criarSugestaoDeJogo(sugestao);

        assertEquals("Player1", resultado.getCriador());
        assertEquals(perfil, resultado.getPerfilCriador());
        assertNotNull(resultado.getDataCriacao());
        verify(sugestaoDeJogoRepository).save(sugestao);
    }

    @Test
    void listarTodasSugestaoDeJogo() {
        when(sugestaoDeJogoRepository.findAll()).thenReturn(List.of(new SugestaoDeJogo()));

        List<SugestaoDeJogo> lista = sugestaoDeJogoService.listarTodasSugestaoDeJogo();
        assertEquals(1, lista.size());
    }

    @Test
    void buscarSugestaoDeJogoPorId_existente() {
        SugestaoDeJogo sugestao = new SugestaoDeJogo();
        sugestao.setId("abc");
        when(sugestaoDeJogoRepository.findById("abc")).thenReturn(Optional.of(sugestao));

        Optional<SugestaoDeJogo> resultado = sugestaoDeJogoService.buscarSugestaoDeJogoPorId("abc");
        assertTrue(resultado.isPresent());
        assertEquals("abc", resultado.get().getId());
    }

    @Test
    void atualizarSugestaoDeJogo_comPermissao() {
        Perfil perfil = new Perfil();
        perfil.setId("123");

        SugestaoDeJogo original = new SugestaoDeJogo();
        original.setId("sug1");
        original.setPerfilCriador(perfil);
        original.setNomeDoJogoSugerido("Old Name");

        SugestaoDeJogo atualizado = new SugestaoDeJogo();
        atualizado.setNomeDoJogoSugerido("Novo Nome");

        when(authentication.getName()).thenReturn("user@email.com");
        when(perfilService.buscarPerfilDoUsuarioLogado("user@email.com")).thenReturn(Optional.of(perfil));
        when(sugestaoDeJogoRepository.findById("sug1")).thenReturn(Optional.of(original));
        when(sugestaoDeJogoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SugestaoDeJogo result = sugestaoDeJogoService.atualizarSugestaoDeJogo("sug1", atualizado);
        assertEquals("Novo Nome", result.getNomeDoJogoSugerido());
    }

    @Test
    void deletarSugestaoDeJogo_comCriador() {
        Perfil perfil = new Perfil();
        perfil.setId("123");

        SugestaoDeJogo sugestao = new SugestaoDeJogo();
        sugestao.setId("sug1");
        sugestao.setPerfilCriador(perfil);

        when(authentication.getName()).thenReturn("user@email.com");
        when(perfilService.buscarPerfilDoUsuarioLogado("user@email.com")).thenReturn(Optional.of(perfil));
        when(sugestaoDeJogoRepository.findById("sug1")).thenReturn(Optional.of(sugestao));

        sugestaoDeJogoService.deletarSugestaoDeJogo("sug1");

        verify(sugestaoDeJogoRepository).deleteById("sug1");
    }

}
