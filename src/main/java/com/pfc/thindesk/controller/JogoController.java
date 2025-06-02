package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.service.JogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jogo")
public class JogoController {

    @Autowired
    private JogoService jogoService;

    // Cria um novo jogo
    @PostMapping
    public Jogo criarJogo(@RequestBody Jogo jogo) {
        return jogoService.criarJogo(jogo);
    }

    // Lista todas os jogos
    @GetMapping
    public List<Jogo> listarJogos() {
        return jogoService.listarTodosJogos();
    }

    // Busca um jogo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Jogo> buscarJogoPorId(@PathVariable String id) {
        return jogoService.buscarJogoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualiza um jogo
    @PutMapping("/{id}")
    public ResponseEntity<Jogo> atualizarJogo(@PathVariable String id, @RequestBody Jogo jogo) {
        Jogo jogoAtualizado = jogoService.atualizarJogo(id, jogo);
        if (jogoAtualizado != null) {
            return ResponseEntity.ok(jogoAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Deleta um jogo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarJogo(@PathVariable String id) {
        jogoService.deletarJogo(id);
        return ResponseEntity.noContent().build();
    }
}
