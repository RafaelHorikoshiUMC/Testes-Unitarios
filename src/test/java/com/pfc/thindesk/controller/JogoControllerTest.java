package com.pfc.thindesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.service.JogoService;
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

@WebMvcTest(JogoController.class)
public class JogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JogoService jogoService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    public class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();
            return http.build();
        }
    }


    private Jogo criarJogoExemplo() {
        return new Jogo("1", "League of Legends", "MOBA", "PC", "Riot Games", "2009");
    }

    @Test
    @WithMockUser
    void testCriarJogo() throws Exception {
        Jogo jogo = criarJogoExemplo();

        Mockito.when(jogoService.criarJogo(any(Jogo.class))).thenReturn(jogo);

        mockMvc.perform(post("/api/jogo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jogo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("League of Legends"));
    }

    @Test
    @WithMockUser
    void testAtualizarJogo() throws Exception {
        Jogo atualizado = new Jogo("1", "LOL Atualizado", "MOBA", "PC", "Riot", "2010");

        Mockito.when(jogoService.atualizarJogo(eq("1"), any(Jogo.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/jogo/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("LOL Atualizado"));
    }


    @Test
    @WithMockUser
    void testListarJogos() throws Exception {
        Jogo jogo1 = criarJogoExemplo();
        Jogo jogo2 = new Jogo("2", "Valorant", "FPS", "PC", "Riot Games", "2020");

        Mockito.when(jogoService.listarTodosJogos()).thenReturn(List.of(jogo1, jogo2));

        mockMvc.perform(get("/api/jogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("League of Legends"))
                .andExpect(jsonPath("$[1].nome").value("Valorant"));
    }

    @Test
    @WithMockUser
    void testBuscarJogoPorId_quandoExistir() throws Exception {
        Jogo jogo = criarJogoExemplo();

        Mockito.when(jogoService.buscarJogoPorId("1")).thenReturn(Optional.of(jogo));

        mockMvc.perform(get("/api/jogo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("League of Legends"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoJogoNaoExistir() throws Exception {
        Mockito.when(jogoService.buscarJogoPorId("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/jogo/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoAtualizarJogoInexistente() throws Exception {
        Jogo qualquer = criarJogoExemplo();

        Mockito.when(jogoService.atualizarJogo(eq("999"), any(Jogo.class))).thenReturn(null);

        mockMvc.perform(put("/api/jogo/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualquer)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeletarJogo() throws Exception {
        Mockito.when(jogoService.deletarJogo("1")).thenReturn(true);

        mockMvc.perform(delete("/api/jogo/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

}
