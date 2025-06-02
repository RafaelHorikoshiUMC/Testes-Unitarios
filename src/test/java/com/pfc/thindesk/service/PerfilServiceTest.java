package com.pfc.thindesk.service;

import com.pfc.thindesk.PerfilMatchDTO;
import com.pfc.thindesk.entity.DecisaoMatch;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.DecisaoMatchRepository;
import com.pfc.thindesk.repository.PerfilRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilServiceTest {

    @InjectMocks
    private PerfilService perfilService;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DecisaoMatchRepository decisaoMatchRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    void mockUsuarioAutenticado(String email, String userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        usuario.setEmail(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
    }

    @Test
    void testCriarPerfil_ComUsuarioAutenticado() {
        mockUsuarioAutenticado("user@email.com", "user123");
        Perfil perfil = new Perfil();
        when(perfilRepository.save(perfil)).thenReturn(perfil);
        Perfil result = perfilService.criarPerfil(perfil);
        assertEquals("user123", result.getUsuarioId());
        verify(perfilRepository).save(perfil);
    }

    @Test
    void testCriarPerfil_SemUsuarioAutenticado() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> perfilService.criarPerfil(new Perfil()));
    }

    @Test
    void testListarPerfisDoUsuario() {
        mockUsuarioAutenticado("email", "user1");

        Perfil perfil1 = new Perfil(); perfil1.setUsuarioId("user1");
        Perfil perfil2 = new Perfil(); perfil2.setUsuarioId("user2");

        when(perfilRepository.findAll()).thenReturn(List.of(perfil1, perfil2));
        List<Perfil> result = perfilService.listarPerfisDoUsuario();
        assertEquals(1, result.size());
    }

    @Test
    void testBuscarPerfilPorId_Autorizado() {
        mockUsuarioAutenticado("email", "user1");
        Perfil perfil = new Perfil(); perfil.setId("1"); perfil.setUsuarioId("user1");
        when(perfilRepository.findById("1")).thenReturn(Optional.of(perfil));
        Optional<Perfil> result = perfilService.buscarPerfilPorId("1");
        assertTrue(result.isPresent());
    }

    @Test
    void testBuscarPerfilPorId_NaoAutorizado() {
        mockUsuarioAutenticado("email", "user1");
        Perfil perfil = new Perfil(); perfil.setId("1"); perfil.setUsuarioId("user2");
        when(perfilRepository.findById("1")).thenReturn(Optional.of(perfil));
        Optional<Perfil> result = perfilService.buscarPerfilPorId("1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testBuscarPerfisPorIds() {
        Perfil p1 = new Perfil(); p1.setId("1");
        when(perfilRepository.findAllById(Set.of("1"))).thenReturn(List.of(p1));
        assertEquals(1, perfilService.buscarPerfisPorIds(Set.of("1")).size());
    }

    @Test
    void testListarTodosPerfis() {
        when(perfilRepository.findAll()).thenReturn(List.of(new Perfil(), new Perfil()));
        assertEquals(2, perfilService.listarTodosPerfis().size());
    }

    @Test
    void testBuscarPerfilPorIdSemRestricao() {
        Perfil perfil = new Perfil();
        when(perfilRepository.findById("1")).thenReturn(Optional.of(perfil));
        assertTrue(perfilService.buscarPerfilPorIdSemRestricao("1").isPresent());
    }

    @Test
    void testAtualizarPerfil_ComAutenticacaoValida() {
        mockUsuarioAutenticado("email", "user1");
        Perfil perfilExistente = new Perfil(); perfilExistente.setId("1"); perfilExistente.setUsuarioId("user1");
        Perfil perfilNovo = new Perfil();

        when(perfilRepository.findById("1")).thenReturn(Optional.of(perfilExistente));
        when(perfilRepository.save(any())).thenReturn(perfilNovo);

        Perfil atualizado = perfilService.atualizarPerfil("1", perfilNovo);
        assertEquals("user1", atualizado.getUsuarioId());
    }

    @Test
    void testAtualizarPerfil_SemAutenticacao() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertThrows(SecurityException.class, () -> perfilService.atualizarPerfil("1", new Perfil()));
    }

    @Test
    void testDeletarPerfil_ComAutenticacaoValida() {
        mockUsuarioAutenticado("email", "user1");
        Perfil perfil = new Perfil(); perfil.setId("1"); perfil.setUsuarioId("user1");
        when(perfilRepository.findById("1")).thenReturn(Optional.of(perfil));

        perfilService.deletarPerfil("1");
        verify(perfilRepository).delete(perfil);
    }

    @Test
    void testBuscarPerfisNaoReagidos() {
        Perfil atual = new Perfil(); atual.setId("A");
        Perfil outro = new Perfil(); outro.setId("B");
        outro.setPlataforma("PC"); atual.setPlataforma("PC");
        outro.setComunicacao("Discord"); atual.setComunicacao("Discord");
        outro.setPeriodoOnline("Noturno"); atual.setPeriodoOnline("Noturno");
        outro.setEstiloDeJogo("Casual"); atual.setEstiloDeJogo("Casual");
        outro.setGeneroPreferidoPrincipal("RPG"); atual.setGeneroPreferidoPrincipal("RPG");
        outro.setGeneroPreferidoSecundario("Ação"); atual.setGeneroPreferidoSecundario("Ação");
        outro.setEstadoCivil("Solteiro"); atual.setEstadoCivil("Solteiro");

        when(decisaoMatchRepository.findByPerfilOrigemId("A")).thenReturn(List.of());
        when(perfilRepository.findAll()).thenReturn(List.of(atual, outro));

        List<PerfilMatchDTO> matches = perfilService.buscarPerfisNaoReagidos(atual);
        assertEquals(1, matches.size());
        assertEquals(100, matches.get(0).scorePercentual());
    }

    @Test
    void testBuscarPerfilDoUsuarioLogado() {
        Usuario usuario = new Usuario(); usuario.setId("1");
        Perfil perfil = new Perfil(); perfil.setUsuarioId("1");

        when(usuarioRepository.findByEmail("email")).thenReturn(Optional.of(usuario));
        when(perfilRepository.findByUsuarioId("1")).thenReturn(Optional.of(perfil));

        Optional<Perfil> result = perfilService.buscarPerfilDoUsuarioLogado("email");
        assertTrue(result.isPresent());
    }

    @Test
    void testBuscarPorUsuario() {
        Usuario usuario = new Usuario(); usuario.setId("123");
        Perfil perfil = new Perfil(); perfil.setUsuarioId("123");
        when(perfilRepository.findByUsuarioId("123")).thenReturn(Optional.of(perfil));
        assertTrue(perfilService.buscarPorUsuario(usuario).isPresent());
    }

    @Test
    void testBuscarPorUsuario_Nulo() {
        assertTrue(perfilService.buscarPorUsuario(null).isEmpty());
        assertTrue(perfilService.buscarPorUsuario(new Usuario()).isEmpty());
    }
}
