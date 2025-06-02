package com.pfc.thindesk.service;

import com.pfc.thindesk.email.EmailService;
import com.pfc.thindesk.email.EmailTemplate;
import com.pfc.thindesk.email.dtos.RecoverDto;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private Map<String, TokenInfo> tokenStorage = new ConcurrentHashMap<>(); // Armazena tokens temporários para recuperação de senha

    // Estrutura auxiliar para guardar token com ID do usuário e data de expiração
    private static class TokenInfo {
        String userId;
        LocalDateTime expiry;

        TokenInfo(String userId, LocalDateTime expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }
    }

    // Envia e-mail com link de recuperação de senha
    public void sendEmailRecoverPassword(RecoverDto dto) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(dto.email())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = UUID.randomUUID().toString(); // Gera token único
            tokenStorage.put(token, new TokenInfo(usuario.getId(), LocalDateTime.now().plusHours(1))); // Salva token com validade

            Context context = new Context();
            String urlReset = "http://localhost:8080/atualizar-senha/" + token; // Link de redefinição
            context.setVariable("nomeUsuario", usuario.getNome());
            context.setVariable("urlReset", urlReset);

            // Envia o e-mail usando o template de recuperação
            emailService.enviarEmail(
                    "Recuperação de senha",
                    usuario.getEmail(),
                    EmailTemplate.RECUPERAR_SENHA,
                    context,
                    Optional.empty()
            );
        } catch (Exception e) {
            e.printStackTrace(); // Exibe erro no console (pode ser melhor tratar com log)
        }
    }

    // Atualiza a senha do usuário com base no token
    public void updatePassword(String token, String novaSenha) {
        TokenInfo info = tokenStorage.get(token);

        if (info == null || info.expiry.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token inválido ou expirado");
        }

        Usuario usuario = usuarioRepository.findById(info.userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha)); // Codifica nova senha
        usuarioRepository.save(usuario); // Salva usuário com senha atualizada

        tokenStorage.remove(token); // Remove token após uso
    }

    // Cria um novo Usuario
    public Usuario criarUsuario(Usuario usuario) {
        if (usuarioRepository.findByNome(usuario.getNome()).isPresent()) {
            throw new RuntimeException("Nome de usuário já está em uso.");
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já está em uso.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    // Busca todos os Usuarios
    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Busca o Usuario por ID
    public Optional<Usuario> buscarUsuarioPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public Usuario buscarPorUsername(String nome) {
        return usuarioRepository.findByNome(nome)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + nome));
    }

    // Atualizar um Usuario
    public Usuario atualizarUsuario(String id, Usuario usuario) {
        Optional<Usuario> existente = usuarioRepository.findById(id);
        if (existente.isPresent()) {
            Usuario atual = existente.get();

            // Evitar mudança para email/nome já existentes em outros usuários
            if (!atual.getEmail().equals(usuario.getEmail()) &&
                    usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                throw new RuntimeException("E-mail já está em uso.");
            }

            if (!atual.getNome().equals(usuario.getNome()) &&
                    usuarioRepository.findByNome(usuario.getNome()).isPresent()) {
                throw new RuntimeException("Nome de usuário já está em uso.");
            }

            usuario.setId(id);

            // Se for atualizar senha, encode novamente
            if (!usuario.getSenha().equals(atual.getSenha())) {
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }

            return usuarioRepository.save(usuario);
        } else {
            return null;
        }
    }

    // Deleta um Usuario
    public void deletarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }
}
