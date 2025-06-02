package com.pfc.thindesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.SugestaoDeJogo;
import com.pfc.thindesk.service.SugestaoDeJogoService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SugestaoDeJogoController.class)
public class SugestaoDeJogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SugestaoDeJogoService sugestaoDeJogoService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();
            return http.build();
        }
    }

    private Perfil criarPerfilExemplo() {
        return new Perfil("perfil1", "gamer123", "10/10/2000", "Aventura", "FPS", "Discord",
                "PC", "Tarde", "competitivo", "legal", "solteiro", "user1", "urlFoto", null);
    }

    private SugestaoDeJogo criarSugestaoExemplo() {
        return new SugestaoDeJogo(
                "sugestao1",
                "Minecraft",
                "Um jogo de construção e aventura",
                "gamer123",
                LocalDateTime.of(2025, 6, 2, 10, 0),
                LocalDateTime.of(2025, 6, 2, 10, 0),
                criarPerfilExemplo()
        );
    }

    @Test
    @WithMockUser
    void testCriarSugestaoDeJogo() throws Exception {
        SugestaoDeJogo sugestao = criarSugestaoExemplo();

        Mockito.when(sugestaoDeJogoService.criarSugestaoDeJogo(any(SugestaoDeJogo.class))).thenReturn(sugestao);

        mockMvc.perform(post("/api/sugestao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sugestao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sugestao1"))
                .andExpect(jsonPath("$.nomeDoJogoSugerido").value("Minecraft"))
                .andExpect(jsonPath("$.criador").value("gamer123"))
                .andExpect(jsonPath("$.perfilCriador.apelido").value("gamer123"));
    }

    @Test
    @WithMockUser
    void testListarTodasSugestoes() throws Exception {
        SugestaoDeJogo s1 = criarSugestaoExemplo();
        SugestaoDeJogo s2 = new SugestaoDeJogo(
                "sugestao2",
                "Valorant",
                "Jogo FPS competitivo",
                "player2",
                LocalDateTime.now(),
                LocalDateTime.now(),
                criarPerfilExemplo()
        );

        Mockito.when(sugestaoDeJogoService.listarTodasSugestaoDeJogo()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/sugestao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nomeDoJogoSugerido").value("Minecraft"))
                .andExpect(jsonPath("$[1].nomeDoJogoSugerido").value("Valorant"));
    }

    @Test
    @WithMockUser
    void testBuscarSugestaoPorId_quandoExistir() throws Exception {
        SugestaoDeJogo sugestao = criarSugestaoExemplo();

        Mockito.when(sugestaoDeJogoService.buscarSugestaoDeJogoPorId("sugestao1")).thenReturn(Optional.of(sugestao));

        mockMvc.perform(get("/api/sugestao/sugestao1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sugestao1"))
                .andExpect(jsonPath("$.nomeDoJogoSugerido").value("Minecraft"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoSugestaoNaoExistir() throws Exception {
        Mockito.when(sugestaoDeJogoService.buscarSugestaoDeJogoPorId("nao-existe")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sugestao/nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testAtualizarSugestaoDeJogo() throws Exception {
        SugestaoDeJogo atualizado = new SugestaoDeJogo(
                "sugestao1",
                "Minecraft Atualizado",
                "Nova descrição",
                "gamer123",
                LocalDateTime.now(),
                LocalDateTime.now(),
                criarPerfilExemplo()
        );

        Mockito.when(sugestaoDeJogoService.atualizarSugestaoDeJogo(eq("sugestao1"), any(SugestaoDeJogo.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/sugestao/sugestao1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeDoJogoSugerido").value("Minecraft Atualizado"))
                .andExpect(jsonPath("$.descricaoOpcional").value("Nova descrição"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoAtualizarSugestaoInexistente() throws Exception {
        SugestaoDeJogo qualquer = criarSugestaoExemplo();

        Mockito.when(sugestaoDeJogoService.atualizarSugestaoDeJogo(eq("nao-existe"), any(SugestaoDeJogo.class))).thenReturn(null);

        mockMvc.perform(put("/api/sugestao/nao-existe")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualquer)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeletarSugestaoDeJogo() throws Exception {
        Mockito.doNothing().when(sugestaoDeJogoService).deletarSugestaoDeJogo("sugestao1");

        mockMvc.perform(delete("/api/sugestao/sugestao1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
