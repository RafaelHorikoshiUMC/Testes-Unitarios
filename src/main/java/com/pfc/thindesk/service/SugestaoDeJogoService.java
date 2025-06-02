package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.SugestaoDeJogo;
import com.pfc.thindesk.repository.SugestaoDeJogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SugestaoDeJogoService {

    @Autowired
    private SugestaoDeJogoRepository sugestaoDeJogoRepository;
    @Autowired
    private PerfilService perfilService;

    // Cria uma nova Sugestao De Jogo
    public SugestaoDeJogo criarSugestaoDeJogo(SugestaoDeJogo sugestaoDeJogo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfil = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        LocalDateTime agora = LocalDateTime.now();
        sugestaoDeJogo.setCriador(perfil.getApelido());
        sugestaoDeJogo.setPerfilCriador(perfil);
        sugestaoDeJogo.setDataCriacao(agora);
        sugestaoDeJogo.setDataAtualizacao(agora);

        return sugestaoDeJogoRepository.save(sugestaoDeJogo);
    }

    // Busca todas as Sugestões De Jogos
    public List<SugestaoDeJogo> listarTodasSugestaoDeJogo() {return sugestaoDeJogoRepository.findAll();}

    // Busca a Sugestao De Jogo por ID
    public Optional<SugestaoDeJogo> buscarSugestaoDeJogoPorId(String id) {
        return sugestaoDeJogoRepository.findById(id);
    }

    // Atualizar uma Sugestao De Jogo
    public SugestaoDeJogo atualizarSugestaoDeJogo(String id, SugestaoDeJogo sugestaoDeJogo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Optional<SugestaoDeJogo> existente = sugestaoDeJogoRepository.findById(id);
        if (existente.isPresent()) {
            SugestaoDeJogo original = existente.get();
            if (!original.getPerfilCriador().getId().equals(perfilLogado.getId())) {
                throw new SecurityException("Você não tem permissão para editar este depoimento.");
            }

            original.setNomeDoJogoSugerido(sugestaoDeJogo.getNomeDoJogoSugerido());
            original.setDataAtualizacao(LocalDateTime.now());
            return sugestaoDeJogoRepository.save(original);
        }
        return null;
    }

    // Deleta uma Sugestao De Jogo
    public void deletarSugestaoDeJogo(String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Optional<SugestaoDeJogo> existente = sugestaoDeJogoRepository.findById(id);
        if (existente.isPresent()) {
            SugestaoDeJogo original = existente.get();

            boolean Criador = original.getPerfilCriador().getId().equals(perfilLogado.getId());
            boolean Admin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!Criador && !Admin) {
                throw new SecurityException("Você não tem permissão para excluir este grupo.");
            }

            sugestaoDeJogoRepository.deleteById(id);
        }
    }

    //Listar Todas as Sugestoes em Paginas
    public Page<SugestaoDeJogo> listarTodasSugestoesPaginadas(Pageable pageable) {
        return sugestaoDeJogoRepository.findAll(pageable);
    }

}
