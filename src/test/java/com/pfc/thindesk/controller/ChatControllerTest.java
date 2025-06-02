package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Grupo;
import com.pfc.thindesk.entity.Mensagem;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.GrupoRepository;
import com.pfc.thindesk.repository.MensagemRepository;
import com.pfc.thindesk.repository.PerfilRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UsuarioRepository usuarioRepository() {
            return Mockito.mock(UsuarioRepository.class);
        }

        @Bean
        public PerfilRepository perfilRepository() {
            return Mockito.mock(PerfilRepository.class);
        }

        @Bean
        public MensagemRepository mensagemRepository() {
            return Mockito.mock(MensagemRepository.class);
        }

        @Bean
        public GrupoRepository grupoRepository() {
            return Mockito.mock(GrupoRepository.class);
        }
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    private Usuario usuario;
    private Perfil perfil;
    private Perfil destinatario;


    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("u1");
        usuario.setEmail("teste@email.com");

        perfil = new Perfil();
        perfil.setId("p1");

        destinatario = new Perfil();
        destinatario.setId("p2");
    }

    @Test
    @WithMockUser(username = "teste@email.com")
    void testAbrirChatPrivadoComSucesso() throws Exception {
        Mockito.when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        Mockito.when(perfilRepository.findByUsuarioId("u1"))
                .thenReturn(Optional.of(perfil));

        Mockito.when(perfilRepository.findById("p2"))
                .thenReturn(Optional.of(destinatario));

        Mockito.when(mensagemRepository
                        .findByRemetenteIdAndDestinatarioIdOrRemetenteIdAndDestinatarioId("p1", "p2", "p2", "p1"))
                .thenReturn(List.of());

        mockMvc.perform(get("/chat/p2"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attributeExists("mensagens", "perfilLogado", "destinatario", "novaMensagem"));
    }

    @Test
    @WithMockUser(username = "teste@email.com")
    void testEnviarMensagemPrivadaComSucesso() throws Exception {
        Mockito.when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));
        Mockito.when(perfilRepository.findByUsuarioId("u1"))
                .thenReturn(Optional.of(perfil));

        mockMvc.perform(post("/chat/enviar")
                        .param("destinatarioId", "p2")
                        .param("conteudo", "Oi")
                        .with(csrf()))  // adiciona o token CSRF
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chat/p2"));
    }



    @Test
    @WithMockUser(username = "teste@email.com")
    void testAbrirChatGrupoComSucesso() throws Exception {
        Grupo grupo = new Grupo();
        grupo.setId("g1");
        grupo.setPerfilCriador(perfil);
        grupo.setParticipantes(List.of()); // não precisa de participantes pois o criador é o logado

        Mockito.when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));
        Mockito.when(perfilRepository.findByUsuarioId("u1"))
                .thenReturn(Optional.of(perfil));
        Mockito.when(grupoRepository.findById("g1"))
                .thenReturn(Optional.of(grupo));
        Mockito.when(mensagemRepository.findByGrupoIdOrderByDataHora("g1"))
                .thenReturn(List.of());

        mockMvc.perform(get("/chat/grupo/g1"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat-grupo"))
                .andExpect(model().attributeExists("grupo", "perfilLogado", "mensagens", "novaMensagem"));
    }

    @Test
    @WithMockUser(username = "teste@email.com")
    void testEnviarMensagemGrupoComSucesso() throws Exception {
        Grupo grupo = new Grupo();
        grupo.setId("g1");
        grupo.setPerfilCriador(perfil);
        grupo.setParticipantes(List.of());

        Mockito.when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));
        Mockito.when(perfilRepository.findByUsuarioId("u1"))
                .thenReturn(Optional.of(perfil));
        Mockito.when(grupoRepository.findById("g1"))
                .thenReturn(Optional.of(grupo));

        mockMvc.perform(post("/chat/grupo/enviar")
                        .param("grupoId", "g1")
                        .param("conteudo", "Olá grupo!")
                        .with(csrf()))  // <-- adiciona o token CSRF aqui
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chat/grupo/g1"));
    }

}
