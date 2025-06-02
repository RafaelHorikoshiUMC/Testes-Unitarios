package com.pfc.thindesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.service.DecisaoMatchService;
import com.pfc.thindesk.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PerfilController.class)
public class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PerfilService perfilService;

    @MockBean
    private DecisaoMatchService decisaoMatchService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // Desabilita CSRF para simplificar testes, ou use .csrf().disable()
            http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();
            return http.build();
        }
    }

    private Perfil criarPerfilExemplo() {
        // Criando um objeto Jogo para jogoPreferido (pode ser mock ou null se não quiser complicar)
        Jogo jogoPreferido = new Jogo();
        jogoPreferido.setId("1");
        jogoPreferido.setNome("Minecraft");

        return new Perfil(
                "1", "gamer123", "10/10/2000", "Aventura", "FPS", "Discord",
                "PC", "Tarde", "competitivo", "legal", "solteiro", "user123", "url_da_foto.jpg",
                jogoPreferido
        );
    }

    @Test
    @WithMockUser
    void testCriarPerfil() throws Exception {
        Perfil perfil = criarPerfilExemplo();

        Mockito.when(perfilService.getUsuarioAutenticado()).thenReturn("user123");
        Mockito.when(perfilService.criarPerfil(any(Perfil.class))).thenReturn(perfil);

        mockMvc.perform(post("/api/perfil")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(perfil)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.apelido").value("gamer123"));
    }

    @Test
    @WithMockUser
    void testListarPerfisDoUsuario() throws Exception {
        Perfil perfil1 = criarPerfilExemplo();
        Perfil perfil2 = new Perfil(
                "2", "player2", "11/11/1995", "MOBA", "RTS", "Teamspeak",
                "Console", "Noite", "casual", "divertido", "casado", "user123", "url_foto2.jpg",
                null // ou outro jogo
        );

        Mockito.when(perfilService.listarPerfisDoUsuario()).thenReturn(List.of(perfil1, perfil2));

        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].apelido").value("gamer123"))
                .andExpect(jsonPath("$[1].apelido").value("player2"));
    }

    @Test
    @WithMockUser
    void testBuscarPerfilPorId_quandoExistir() throws Exception {
        Perfil perfil = criarPerfilExemplo();

        Mockito.when(perfilService.buscarPerfilPorId("1")).thenReturn(Optional.of(perfil));

        mockMvc.perform(get("/api/perfil/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.apelido").value("gamer123"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoPerfilNaoExistir() throws Exception {
        Mockito.when(perfilService.buscarPerfilPorId("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/perfil/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testAtualizarPerfil() throws Exception {
        Perfil atualizado = new Perfil(
                "2", "player3", "11/11/1995", "MOBA", "RTS", "Teamspeak",
                "Console", "Noite", "casual", "divertido", "casado", "user123", "url_foto2.jpg", null
        );

        Mockito.when(perfilService.atualizarPerfil(eq("1"), any(Perfil.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/perfil/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apelido").value("player3"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoAtualizarPerfilInexistente() throws Exception {
        Perfil qualquer = criarPerfilExemplo();

        Mockito.when(perfilService.atualizarPerfil(eq("999"), any(Perfil.class))).thenReturn(null);

        mockMvc.perform(put("/api/perfil/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualquer)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeletarPerfil() throws Exception {
        Mockito.doNothing().when(perfilService).deletarPerfil("1");

        mockMvc.perform(delete("/api/perfil/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
