package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.service.DepoimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/depoimento")
public class DepoimentoController {
    @Autowired
    private DepoimentoService depoimentoService;

    // Cria um novo Depoimento
    @PostMapping
    public Depoimento criarDepoimento(@RequestBody Depoimento depoimento) {
        return depoimentoService.criarDepoimento(depoimento);
    }

    // Lista todas os Depoimentos
    @GetMapping
    public List<Depoimento> listarDepoimentos() {
        return depoimentoService.listarTodosDepoimentos();
    }

    // Busca um Depoimento por ID
    @GetMapping("/{id}")
    public ResponseEntity<Depoimento> buscarDepoimentoPorId(@PathVariable String id) {
        return depoimentoService.buscarDepoimentoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualiza um Depoimento
    @PutMapping("/{id}")
    public ResponseEntity<Depoimento> atualizarDepoimento(@PathVariable String id, @RequestBody Depoimento depoimento) {
        Depoimento depoimentoAtualizado = depoimentoService.atualizarDepoimento(id, depoimento);
        if (depoimentoAtualizado != null) {
            return ResponseEntity.ok(depoimentoAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Deleta um Depoimento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarDepoimento(@PathVariable String id) {
        depoimentoService.deletarDepoimento(id);
        return ResponseEntity.noContent().build();
    }
}
