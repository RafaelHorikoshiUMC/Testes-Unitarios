package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.repository.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JogoService {

    @Autowired
    private JogoRepository jogoRepository;

    // Cria uma novo jogo
    public Jogo criarJogo(Jogo jogo) {
        return jogoRepository.save(jogo);
    }

    // Busca todos os jogos
    public List<Jogo> listarTodosJogos() {
        return jogoRepository.findAll();
    }

    // Busca o jogo por ID
    public Optional<Jogo> buscarJogoPorId(String id) {
        return jogoRepository.findById(id);
    }

    // Atualizar um jogo
    public Jogo atualizarJogo(String id, Jogo jogo) {
        if (jogoRepository.existsById(id)) {
            jogo.setId(id);
            return jogoRepository.save(jogo);
        } else {
            return null;
        }
    }

    // Deleta um jogo
    public boolean deletarJogo(String id) {
        if (jogoRepository.existsById(id)) {
            jogoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    //Busca Jogos nas Paginas
    public Page<Jogo> buscarJogosPaginados(String termo, Pageable pageable) {
        if (termo != null && !termo.trim().isEmpty()) {
            return jogoRepository.findByNomeContainingIgnoreCase(termo, pageable);
        } else {
            return jogoRepository.findAll(pageable);
        }
    }
}
