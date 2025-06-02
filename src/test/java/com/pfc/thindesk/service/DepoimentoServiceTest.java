package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.repository.DepoimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class DepoimentoServiceTest {

    @InjectMocks
    private DepoimentoService depoimentoService;

    @Mock
    private DepoimentoRepository depoimentoRepository;

    @Mock
    private PerfilService perfilService;

    private Perfil perfil;
    private Depoimento depoimento;

    @BeforeEach
    void setUp() {
        perfil = new Perfil();
        perfil.setId("1");
        perfil.setApelido("João");

        depoimento = new Depoimento();
        depoimento.setId("dep1");
        depoimento.setTexto("Texto original");

        // Simular usuário logado
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario@email.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Test
    void testCriarDepoimento() {
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));
        when(depoimentoRepository.save(any(Depoimento.class))).thenAnswer(i -> i.getArgument(0));

        Depoimento novo = new Depoimento();
        novo.setTexto("Novo depoimento");

        Depoimento criado = depoimentoService.criarDepoimento(novo);

        assertEquals("João", criado.getCriador());
        assertEquals(perfil, criado.getPerfilCriador());
        assertNotNull(criado.getDataCriacao());
        assertNotNull(criado.getDataAtualizacao());
    }

    @Test
    void testListarTodosDepoimentos() {
        List<Depoimento> lista = Arrays.asList(new Depoimento(), new Depoimento());
        when(depoimentoRepository.findAll()).thenReturn(lista);

        List<Depoimento> result = depoimentoService.listarTodosDepoimentos();
        assertEquals(2, result.size());
    }

    @Test
    void testBuscarDepoimentoPorId() {
        when(depoimentoRepository.findById("dep1")).thenReturn(Optional.of(depoimento));

        Optional<Depoimento> result = depoimentoService.buscarDepoimentoPorId("dep1");
        assertTrue(result.isPresent());
        assertEquals("dep1", result.get().getId());
    }

    @Test
    void testAtualizarDepoimento_ComPermissao() {
        Depoimento atualizado = new Depoimento();
        atualizado.setTexto("Texto atualizado");

        depoimento.setPerfilCriador(perfil);

        when(depoimentoRepository.findById("dep1")).thenReturn(Optional.of(depoimento));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));
        when(depoimentoRepository.save(any(Depoimento.class))).thenAnswer(i -> i.getArgument(0));

        Depoimento result = depoimentoService.atualizarDepoimento("dep1", atualizado);

        assertEquals("Texto atualizado", result.getTexto());
        assertNotNull(result.getDataAtualizacao());
    }

    @Test
    void testAtualizarDepoimento_SemPermissao() {
        Perfil outroPerfil = new Perfil();
        outroPerfil.setId("outro");

        depoimento.setPerfilCriador(outroPerfil);

        when(depoimentoRepository.findById("dep1")).thenReturn(Optional.of(depoimento));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertThrows(SecurityException.class, () -> {
            depoimentoService.atualizarDepoimento("dep1", new Depoimento());
        });
    }

    @Test
    void testDeletarDepoimento_ComPermissaoDoCriador() {
        depoimento.setPerfilCriador(perfil);

        when(depoimentoRepository.findById("dep1")).thenReturn(Optional.of(depoimento));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertDoesNotThrow(() -> depoimentoService.deletarDepoimento("dep1"));
        verify(depoimentoRepository).deleteById("dep1");
    }

    @Test
    void testDeletarDepoimento_ComPermissaoDeAdmin() {
        Perfil outroPerfil = new Perfil();
        outroPerfil.setId("outro");
        depoimento.setPerfilCriador(outroPerfil);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@email.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        when(depoimentoRepository.findById("dep1")).thenReturn(Optional.of(depoimento));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertDoesNotThrow(() -> depoimentoService.deletarDepoimento("dep1"));
        verify(depoimentoRepository).deleteById("dep1");
    }

    @Test
    void testDeletarDepoimento_SemPermissao() {
        Perfil outroPerfil = new Perfil();
        outroPerfil.setId("outro");
        depoimento.setPerfilCriador(outroPerfil);

        when(depoimentoRepository.findById("dep1")).thenReturn(Optional.of(depoimento));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertThrows(SecurityException.class, () -> {
            depoimentoService.deletarDepoimento("dep1");
        });
    }
}
