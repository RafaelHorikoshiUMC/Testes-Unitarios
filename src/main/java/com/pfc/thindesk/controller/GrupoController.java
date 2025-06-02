package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Grupo;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.service.GrupoService;
import com.pfc.thindesk.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupo")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;
    @Autowired
    private PerfilService perfilService;

    // Cria um novo grupo
    @PostMapping
    public Grupo criarGrupo(@RequestBody Grupo grupo) {
        if (grupo.getLimiteParticipantes() <= 0) {
            grupo.setLimiteParticipantes(5); // valor padrão
        }
        return grupoService.criarGrupo(grupo);
    }

    // Lista todos os grupos
    @GetMapping
    public List<Grupo> listarGrupos() {
        return grupoService.listarTodosGrupos();
    }

    // Busca um grupo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Grupo> buscarGrupoPorId(@PathVariable String id) {
        return grupoService.buscarGrupoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualiza um grupo
    @PutMapping("/{id}")
    public ResponseEntity<Grupo> atualizarGrupo(@PathVariable String id, @RequestBody Grupo grupo) {
        Grupo grupoAtualizado = grupoService.atualizarGrupo(id, grupo);
        if (grupoAtualizado != null) {
            return ResponseEntity.ok(grupoAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Deleta um grupo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarGrupo(@PathVariable String id) {
        grupoService.deletarGrupo(id);
        return ResponseEntity.noContent().build();
    }


}
