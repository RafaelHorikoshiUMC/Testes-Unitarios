package com.pfc.thindesk.service;

import com.pfc.thindesk.email.EmailService;
import com.pfc.thindesk.email.EmailTemplate;
import com.pfc.thindesk.email.dtos.RecoverDto;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void criarUsuario_Sucesso() {
        Usuario usuario = new Usuario();
        usuario.setNome("usuarioTeste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("12345");

        when(usuarioRepository.findByNome("usuarioTeste")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345")).thenReturn("senhaCodificada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario criado = usuarioService.criarUsuario(usuario);

        assertNotNull(criado);
        assertEquals("senhaCodificada", criado.getSenha());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void criarUsuario_NomeDuplicado_Excecao() {
        Usuario usuario = new Usuario();
        usuario.setNome("usuarioTeste");

        when(usuarioRepository.findByNome("usuarioTeste")).thenReturn(Optional.of(new Usuario()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.criarUsuario(usuario));
        assertEquals("Nome de usuário já está em uso.", ex.getMessage());
    }

    @Test
    void sendEmailRecoverPassword_Sucesso() {
        Usuario usuario = new Usuario();
        usuario.setId("id1");
        usuario.setNome("Nome");
        usuario.setEmail("email@test.com");

        RecoverDto dto = new RecoverDto("email@test.com");

        when(usuarioRepository.findByEmail("email@test.com")).thenReturn(Optional.of(usuario));

        usuarioService.sendEmailRecoverPassword(dto);

        verify(emailService).enviarEmail(
                eq("Recuperação de senha"),
                eq("email@test.com"),
                eq(EmailTemplate.RECUPERAR_SENHA),
                any(Context.class),
                eq(Optional.empty())
        );
    }

    @Test
    void listarTodosUsuarios_RetornaLista() {
        List<Usuario> usuarios = List.of(new Usuario(), new Usuario());
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<Usuario> resultado = usuarioService.listarTodosUsuarios();

        assertEquals(2, resultado.size());
        verify(usuarioRepository).findAll();
    }

    @Test
    void buscarUsuarioPorId_Encontrado() {
        Usuario usuario = new Usuario();
        usuario.setId("123");
        when(usuarioRepository.findById("123")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarUsuarioPorId("123");

        assertTrue(resultado.isPresent());
        assertEquals("123", resultado.get().getId());
        verify(usuarioRepository).findById("123");
    }

    @Test
    void buscarPorEmail_Encontrado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@email.com");
        when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorEmail("teste@email.com");

        assertEquals("teste@email.com", resultado.getEmail());
        verify(usuarioRepository).findByEmail("teste@email.com");
    }

    @Test
    void buscarPorEmail_NaoEncontrado_LancaExcecao() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.buscarPorEmail("naoexiste@email.com"));

        verify(usuarioRepository).findByEmail("naoexiste@email.com");
    }

    @Test
    void buscarPorUsername_Encontrado() {
        Usuario usuario = new Usuario();
        usuario.setNome("usuario1");
        when(usuarioRepository.findByNome("usuario1")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorUsername("usuario1");

        assertEquals("usuario1", resultado.getNome());
        verify(usuarioRepository).findByNome("usuario1");
    }

    @Test
    void buscarPorUsername_NaoEncontrado_LancaExcecao() {
        when(usuarioRepository.findByNome("invalido")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.buscarPorUsername("invalido"));

        verify(usuarioRepository).findByNome("invalido");
    }

    @Test
    void atualizarUsuario_Sucesso() {
        Usuario existente = new Usuario();
        existente.setId("123");
        existente.setEmail("email@antigo.com");
        existente.setNome("usuarioAntigo");
        existente.setSenha("senhaCodificada");

        Usuario atualizado = new Usuario();
        atualizado.setEmail("email@novo.com");
        atualizado.setNome("usuarioNovo");
        atualizado.setSenha("novaSenha");

        when(usuarioRepository.findById("123")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail("email@novo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByNome("usuarioNovo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("novaSenha")).thenReturn("senhaCodificadaNova");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.atualizarUsuario("123", atualizado);

        assertNotNull(resultado);
        assertEquals("123", resultado.getId());
        assertEquals("email@novo.com", resultado.getEmail());
        assertEquals("usuarioNovo", resultado.getNome());
        assertEquals("senhaCodificadaNova", resultado.getSenha());

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void atualizarUsuario_EmailDuplicado_Excecao() {
        Usuario existente = new Usuario();
        existente.setId("123");
        existente.setEmail("email@antigo.com");
        existente.setNome("usuarioAntigo");
        existente.setSenha("senhaCodificada");

        Usuario atualizado = new Usuario();
        atualizado.setEmail("email@outro.com");
        atualizado.setNome("usuarioNovo");
        atualizado.setSenha("novaSenha");

        when(usuarioRepository.findById("123")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail("email@outro.com")).thenReturn(Optional.of(new Usuario()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.atualizarUsuario("123", atualizado));
        assertEquals("E-mail já está em uso.", ex.getMessage());
    }

    @Test
    void atualizarUsuario_NomeDuplicado_Excecao() {
        Usuario existente = new Usuario();
        existente.setId("123");
        existente.setEmail("email@antigo.com");
        existente.setNome("usuarioAntigo");
        existente.setSenha("senhaCodificada");

        Usuario atualizado = new Usuario();
        atualizado.setEmail("email@antigo.com");
        atualizado.setNome("usuarioOutro");
        atualizado.setSenha("novaSenha");

        when(usuarioRepository.findById("123")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail("email@antigo.com")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByNome("usuarioOutro")).thenReturn(Optional.of(new Usuario()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.atualizarUsuario("123", atualizado));
        assertEquals("Nome de usuário já está em uso.", ex.getMessage());
    }

    @Test
    void atualizarUsuario_NaoEncontrado_RetornaNull() {
        Usuario atualizado = new Usuario();
        atualizado.setEmail("email@novo.com");
        atualizado.setNome("usuarioNovo");
        atualizado.setSenha("novaSenha");

        when(usuarioRepository.findById("123")).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.atualizarUsuario("123", atualizado);

        assertNull(resultado);
    }

    @Test
    void deletarUsuario_ChamaDelete() {
        usuarioService.deletarUsuario("id1");

        verify(usuarioRepository).deleteById("id1");
    }
}
