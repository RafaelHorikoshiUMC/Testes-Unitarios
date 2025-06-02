package com.pfc.thindesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfc.thindesk.email.dtos.RecoverDto;
import com.pfc.thindesk.email.dtos.RecoverPasswordDto;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.service.UsuarioService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

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

    private Usuario criarUsuarioExemplo() {
        return new Usuario(
                "1", "usuario1", "senha123", "usuario1@email.com", "ROLE_USER", null, null
        );
    }

    @Test
    @WithMockUser
    void testCriarUsuario() throws Exception {
        Usuario usuario = criarUsuarioExemplo();

        Mockito.when(usuarioService.criarUsuario(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("usuario1"))
                .andExpect(jsonPath("$.email").value("usuario1@email.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @WithMockUser
    void testBuscarUsuarioPorId_quandoExistir() throws Exception {
        Usuario usuario = criarUsuarioExemplo();

        Mockito.when(usuarioService.buscarUsuarioPorId("1")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("usuario1"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoUsuarioNaoExistir() throws Exception {
        Mockito.when(usuarioService.buscarUsuarioPorId("nao-existe")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuario/nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testAtualizarUsuario_quandoExistir() throws Exception {
        Usuario usuarioAtualizado = criarUsuarioExemplo();
        usuarioAtualizado.setNome("novoNome");

        Mockito.when(usuarioService.atualizarUsuario(eq("1"), any(Usuario.class))).thenReturn(usuarioAtualizado);

        mockMvc.perform(post("/api/usuario/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("novoNome"));
    }

    @Test
    @WithMockUser
    void testRetornar404_quandoAtualizarUsuarioInexistente() throws Exception {
        Usuario usuario = criarUsuarioExemplo();

        Mockito.when(usuarioService.atualizarUsuario(eq("nao-existe"), any(Usuario.class))).thenReturn(null);

        mockMvc.perform(post("/api/usuario/nao-existe")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeletarUsuario() throws Exception {
        Mockito.doNothing().when(usuarioService).deletarUsuario("1");

        mockMvc.perform(delete("/api/usuario/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testEnviarEmailRecuperacaoSenha() throws Exception {
        RecoverDto dto = new RecoverDto("usuario1@email.com");

        Mockito.doNothing().when(usuarioService).sendEmailRecoverPassword(any(RecoverDto.class));

        mockMvc.perform(post("/api/usuario/recuperar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("E-mail de recuperação enviado."));
    }

    @Test
    @WithMockUser
    void testAtualizarSenha() throws Exception {
        RecoverPasswordDto dto = new RecoverPasswordDto("novaSenha123");

        Mockito.doNothing().when(usuarioService).updatePassword(eq("token123"), eq("novaSenha123"));

        mockMvc.perform(patch("/api/usuario/atualizar-senha/token123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Senha atualizada com sucesso."));
    }
}
