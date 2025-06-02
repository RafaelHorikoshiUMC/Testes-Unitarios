package com.pfc.thindesk.controller;

import com.pfc.thindesk.MatchDTO;
import com.pfc.thindesk.PerfilMatchDTO;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.service.DecisaoMatchService;
import com.pfc.thindesk.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private DecisaoMatchService decisaoMatchService;
    // Cria um novo perfil
    @PostMapping
    public ResponseEntity<?> criarPerfil(@RequestBody Perfil perfil) {
        try {
            // Define o usuário autenticado no perfil antes de salvar
            String usuarioIdLogado = perfilService.getUsuarioAutenticado();
            if (usuarioIdLogado == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Usuário não está autenticado.");
            }
            perfil.setUsuarioId(usuarioIdLogado);

            Perfil novoPerfil = perfilService.criarPerfil(perfil);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoPerfil);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Já existe um perfil associado a este usuário.");
        }
    }


    // Lista todos os perfis
    @GetMapping
    public ResponseEntity<List<Perfil>> listarPerfisDoUsuario() {
        List<Perfil> perfis = perfilService.listarPerfisDoUsuario();
        return ResponseEntity.ok(perfis);
    }


    // Busca um perfil por ID
    @GetMapping("/{id}")
    public ResponseEntity<Perfil> buscarPerfilPorId(@PathVariable String id) {
        return perfilService.buscarPerfilPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualiza um perfil
    @PutMapping("/{id}")
    public ResponseEntity<Perfil> atualizarPerfil(@PathVariable String id, @RequestBody Perfil perfil) {
        Perfil perfilAtualizado = perfilService.atualizarPerfil(id, perfil);
        if (perfilAtualizado != null) {
            return ResponseEntity.ok(perfilAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Deleta um perfil
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPerfil(@PathVariable String id) {
        perfilService.deletarPerfil(id);
        return ResponseEntity.noContent().build();
    }
}
