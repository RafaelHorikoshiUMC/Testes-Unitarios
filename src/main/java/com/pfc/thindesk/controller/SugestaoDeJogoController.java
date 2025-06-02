package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.SugestaoDeJogo;
import com.pfc.thindesk.service.SugestaoDeJogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sugestao")
public class SugestaoDeJogoController {

    @Autowired
    private SugestaoDeJogoService sugestaoDeJogoService;

    // Cria uma nova Sugestao De Jogo
    @PostMapping
    public SugestaoDeJogo criarSugestaoDeJogo(@RequestBody SugestaoDeJogo sugestaoDeJogo) {
        return sugestaoDeJogoService.criarSugestaoDeJogo(sugestaoDeJogo);
    }

    // Lista todas as Sugestões De Jogos
    @GetMapping
    public List<SugestaoDeJogo> listarSugestaoDeJogo() {
        return sugestaoDeJogoService.listarTodasSugestaoDeJogo();
    }

    // Busca uma Sugestao De Jogo por ID
    @GetMapping("/{id}")
    public ResponseEntity<SugestaoDeJogo> buscarSugestaoDeJogoPorId(@PathVariable String id) {
        return sugestaoDeJogoService.buscarSugestaoDeJogoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualiza uma Sugestao De Jogo
    @PutMapping("/{id}")
    public ResponseEntity<SugestaoDeJogo> atualizarSugestaoDeJogo(@PathVariable String id, @RequestBody SugestaoDeJogo sugestaoDeJogo) {
        SugestaoDeJogo sugestaoDeJogoAtualizado = sugestaoDeJogoService.atualizarSugestaoDeJogo (id, sugestaoDeJogo);
        if (sugestaoDeJogoAtualizado != null) {
            return ResponseEntity.ok(sugestaoDeJogoAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Deleta uma Sugestao De Jogo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSugestaoDeJogo(@PathVariable String id) {
        sugestaoDeJogoService.deletarSugestaoDeJogo(id);
        return ResponseEntity.noContent().build();
    }
}
