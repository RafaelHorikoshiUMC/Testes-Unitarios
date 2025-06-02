package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Grupo;
import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.repository.GrupoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class GrupoServiceTest {

    @InjectMocks
    private GrupoService grupoService;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private PerfilService perfilService;

    private Perfil perfil;
    private Grupo grupo;

    @BeforeEach
    void setUp() {
        perfil = new Perfil();
        perfil.setId("1");
        perfil.setApelido("Usuário");

        grupo = new Grupo();
        grupo.setId("grupo1");
        grupo.setPerfilCriador(perfil);
        grupo.setParticipantes(new ArrayList<>());
        grupo.setLimiteParticipantes(5);
        grupo.setJogo(new Jogo());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario@email.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Test
    void testCriarGrupo() {
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(i -> i.getArgument(0));

        Grupo novo = new Grupo();
        novo.setParticipantes(new ArrayList<>());
        novo.setLimiteParticipantes(5);

        Grupo criado = grupoService.criarGrupo(novo);

        assertEquals("Usuário", criado.getCriador());
        assertTrue(criado.getParticipantes().contains(perfil));
    }

    @Test
    void testListarTodosGrupos() {
        when(grupoRepository.findAll()).thenReturn(List.of(new Grupo(), new Grupo()));

        List<Grupo> grupos = grupoService.listarTodosGrupos();
        assertEquals(2, grupos.size());
    }

    @Test
    void testBuscarGrupoPorId() {
        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));

        Optional<Grupo> result = grupoService.buscarGrupoPorId("grupo1");
        assertTrue(result.isPresent());
        assertEquals("grupo1", result.get().getId());
    }

    @Test
    void testAtualizarGrupo_ComPermissao() {
        Grupo atualizado = new Grupo();
        Jogo novoJogo = new Jogo();
        atualizado.setJogo(novoJogo);

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(i -> i.getArgument(0));

        Grupo result = grupoService.atualizarGrupo("grupo1", atualizado);
        assertEquals(novoJogo, result.getJogo());
    }

    @Test
    void testAtualizarGrupo_SemPermissao() {
        Perfil outro = new Perfil();
        outro.setId("2");
        grupo.setPerfilCriador(outro);

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertThrows(SecurityException.class, () -> grupoService.atualizarGrupo("grupo1", new Grupo()));
    }

    @Test
    void testDeletarGrupo_Criador() {
        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertDoesNotThrow(() -> grupoService.deletarGrupo("grupo1"));
        verify(grupoRepository).deleteById("grupo1");
    }

    @Test
    void testDeletarGrupo_Admin() {
        Perfil outro = new Perfil();
        outro.setId("2");
        grupo.setPerfilCriador(outro);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@email.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertDoesNotThrow(() -> grupoService.deletarGrupo("grupo1"));
        verify(grupoRepository).deleteById("grupo1");
    }

    @Test
    void testDeletarGrupo_SemPermissao() {
        Perfil outro = new Perfil();
        outro.setId("2");
        grupo.setPerfilCriador(outro);

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertThrows(SecurityException.class, () -> grupoService.deletarGrupo("grupo1"));
    }

    @Test
    void testEntrarNoGrupo_Sucesso() {
        grupo.setParticipantes(new ArrayList<>());

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(i -> i.getArgument(0));

        grupoService.entrarNoGrupo("grupo1");

        assertTrue(grupo.getParticipantes().contains(perfil));
    }

    @Test
    void testEntrarNoGrupo_JaParticipa() {
        grupo.setParticipantes(new ArrayList<>(List.of(perfil)));

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        grupoService.entrarNoGrupo("grupo1");

        // Verifica que o participante não foi adicionado novamente
        assertEquals(1, grupo.getParticipantes().size());
    }

    @Test
    void testEntrarNoGrupo_LimiteExcedido() {
        List<Perfil> cheios = new ArrayList<>();
        for (int i = 0; i < grupo.getLimiteParticipantes(); i++) {
            Perfil p = new Perfil();
            p.setId("p" + i);
            cheios.add(p);
        }
        grupo.setParticipantes(cheios);

        when(grupoRepository.findById("grupo1")).thenReturn(Optional.of(grupo));
        when(perfilService.buscarPerfilDoUsuarioLogado(anyString())).thenReturn(Optional.of(perfil));

        assertThrows(IllegalStateException.class, () -> grupoService.entrarNoGrupo("grupo1"));
    }
}
