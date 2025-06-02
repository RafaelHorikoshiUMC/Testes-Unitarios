package com.pfc.thindesk.controller;


import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.service.DepoimentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(DepoimentoController.class)
@Import(DepoimentoControllerTest.TestSecurityConfig.class)
class DepoimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // <--- mocka o serviço e injeta no contexto, sem tentar criar o real
    private DepoimentoService depoimentoService;

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
    @WithMockUser  // Simula um usuário autenticado no contexto do Spring Security
    void testCriarDepoimento() throws Exception {
        Depoimento depoimento = new Depoimento();
        depoimento.setId("1");
        depoimento.setTexto("Teste depoimento");

        Mockito.when(depoimentoService.criarDepoimento(any(Depoimento.class))).thenReturn(depoimento);

        mockMvc.perform(post("/api/depoimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depoimento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.texto").value("Teste depoimento"));
    }


    @Test
    @WithMockUser
    void testListarDepoimentos() throws Exception {
        Depoimento depoimento1 = new Depoimento();
        depoimento1.setId("1");
        depoimento1.setTexto("Depoimento 1");

        Depoimento depoimento2 = new Depoimento();
        depoimento2.setId("2");
        depoimento2.setTexto("Depoimento 2");

        Mockito.when(depoimentoService.listarTodosDepoimentos()).thenReturn(List.of(depoimento1, depoimento2));

        mockMvc.perform(get("/api/depoimento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    @WithMockUser
    void testBuscarDepoimentoPorId_quandoExistir() throws Exception {
        Depoimento depoimento = new Depoimento();
        depoimento.setId("1");
        depoimento.setTexto("Depoimento existente");

        Mockito.when(depoimentoService.buscarDepoimentoPorId("1")).thenReturn(Optional.of(depoimento));

        mockMvc.perform(get("/api/depoimento/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.texto").value("Depoimento existente"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoBuscarDepoimentoPorIdInexistente() throws Exception {
        Mockito.when(depoimentoService.buscarDepoimentoPorId("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/depoimento/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testAtualizarDepoimento_quandoExistir() throws Exception {
        Depoimento depoimentoAtualizado = new Depoimento();
        depoimentoAtualizado.setId("1");
        depoimentoAtualizado.setTexto("Atualizado");

        Mockito.when(depoimentoService.atualizarDepoimento(eq("1"), any(Depoimento.class)))
                .thenReturn(depoimentoAtualizado);

        mockMvc.perform(put("/api/depoimento/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depoimentoAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("Atualizado"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoAtualizarDepoimentoInexistente() throws Exception {
        Depoimento depoimento = new Depoimento();
        depoimento.setTexto("Qualquer texto");

        Mockito.when(depoimentoService.atualizarDepoimento(eq("999"), any(Depoimento.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/depoimento/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depoimento)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeletarDepoimento() throws Exception {
        // Suponha que deletarDepoimento retorna void, então só vamos mockar sem lançar exceção
        Mockito.doNothing().when(depoimentoService).deletarDepoimento("1");

        mockMvc.perform(delete("/api/depoimento/1"))
                .andExpect(status().isNoContent());
    }
}
