package com.pfc.thindesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfc.thindesk.entity.Grupo;
import com.pfc.thindesk.service.GrupoService;
import com.pfc.thindesk.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GrupoController.class)
@Import(GrupoControllerTest.TestSecurityConfig.class)
class GrupoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrupoService grupoService;

    @MockBean
    private PerfilService perfilService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    public static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();
            return http.build();
        }
    }

    @Test
    @WithMockUser
    void testCriarGrupo() throws Exception {
        Grupo grupo = new Grupo();
        grupo.setId("1");
        grupo.setCriador("user123");
        grupo.setLimiteParticipantes(3);

        Mockito.when(grupoService.criarGrupo(any(Grupo.class))).thenReturn(grupo);

        mockMvc.perform(post("/api/grupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.criador").value("user123"))
                .andExpect(jsonPath("$.limiteParticipantes").value(3));
    }

    @Test
    @WithMockUser
    void testListarGrupos() throws Exception {
        Grupo grupo1 = new Grupo();
        grupo1.setId("1");
        grupo1.setCriador("Grupo 1");

        Grupo grupo2 = new Grupo();
        grupo2.setId("2");
        grupo2.setCriador("Grupo 2");

        Mockito.when(grupoService.listarTodosGrupos()).thenReturn(List.of(grupo1, grupo2));

        mockMvc.perform(get("/api/grupo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    @WithMockUser
    void testBuscarGrupoPorId_quandoExistir() throws Exception {
        Grupo grupo = new Grupo();
        grupo.setId("1");
        grupo.setCriador("user123");

        Mockito.when(grupoService.buscarGrupoPorId("1")).thenReturn(Optional.of(grupo));

        mockMvc.perform(get("/api/grupo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.criador").value("user123"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoBuscarGrupoPorIdInexistente() throws Exception {
        Mockito.when(grupoService.buscarGrupoPorId("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/grupo/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testAtualizarGrupo_quandoExistir() throws Exception {
        Grupo grupoAtualizado = new Grupo();
        grupoAtualizado.setId("1");
        grupoAtualizado.setCriador("user123");

        Mockito.when(grupoService.atualizarGrupo(eq("1"), any(Grupo.class)))
                .thenReturn(grupoAtualizado);

        mockMvc.perform(put("/api/grupo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupoAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criador").value("user123"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoAtualizarGrupoInexistente() throws Exception {
        Grupo grupo = new Grupo();
        grupo.setCriador("Novo nome");

        Mockito.when(grupoService.atualizarGrupo(eq("999"), any(Grupo.class))).thenReturn(null);

        mockMvc.perform(put("/api/grupo/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeletarGrupo() throws Exception {
        Mockito.doNothing().when(grupoService).deletarGrupo("1");

        mockMvc.perform(delete("/api/grupo/1"))
                .andExpect(status().isNoContent());
    }
}
