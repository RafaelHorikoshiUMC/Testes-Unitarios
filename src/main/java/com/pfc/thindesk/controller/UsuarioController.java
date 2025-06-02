package com.pfc.thindesk.controller;

import com.pfc.thindesk.email.dtos.RecoverDto;
import com.pfc.thindesk.email.dtos.RecoverPasswordDto;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    //Cria um novo Usuario
    @PostMapping
    public Usuario criarUsuario(@RequestBody Usuario usuario) {
        usuario.setRole("ROLE_USER");
        return usuarioService.criarUsuario(usuario);
    }

    // Recebe o e-mail e envia o link de recuperação de senha (via API)
    @PostMapping("/recuperar")
    public ResponseEntity<String> recover(@RequestBody RecoverDto dto) {
        usuarioService.sendEmailRecoverPassword(dto); // envia o e-mail com link
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("E-mail de recuperação enviado.");
    }

    // Atualiza a senha usando o token (via API)
    @PatchMapping("/atualizar-senha/{token}")
    public ResponseEntity<String> updatePassword(
            @PathVariable String token,
            @RequestBody RecoverPasswordDto dto
    ) {
        usuarioService.updatePassword(token, dto.senha()); // atualiza a senha no banco
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Senha atualizada com sucesso.");
    }

    // Busca um usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarUsuarioPorId(@PathVariable String id) {
        return usuarioService.buscarUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualiza um usuario
    @PostMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        Usuario usuarioAtualizado = usuarioService.atualizarUsuario(id, usuario);
        if (usuarioAtualizado != null) {
            return ResponseEntity.ok(usuarioAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Deleta um usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable String id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
