package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.entity.Grupo;
import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.repository.GrupoRepository;
import com.pfc.thindesk.repository.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;
    @Autowired
    private PerfilService perfilService;

    // Cria um novo grupo
    public Grupo criarGrupo(Grupo grupo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfil = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        grupo.setCriador(perfil.getApelido());
        grupo.setPerfilCriador(perfil);

        // Adiciona o criador à lista de participantes, se ainda não estiver
        if (!grupo.getParticipantes().contains(perfil)) {
            grupo.getParticipantes().add(perfil);
        }

        return grupoRepository.save(grupo);
    }


    // Busca todos os grupos
    public List<Grupo> listarTodosGrupos() {
        return grupoRepository.findAll();
    }

    // Busca o grupo por ID
    public Optional<Grupo> buscarGrupoPorId(String id) {
        return grupoRepository.findById(id);
    }

    // Atualizar um grupo
    public Grupo atualizarGrupo(String id, Grupo grupo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Optional<Grupo> existente = grupoRepository.findById(id);
        if (existente.isPresent()) {
            Grupo original = existente.get();
            if (!original.getPerfilCriador().getId().equals(perfilLogado.getId())) {
                throw new SecurityException("Você não tem permissão para editar este grupo.");
            }

            original.setJogo(grupo.getJogo());
            return grupoRepository.save(original);
        }
        return null;
    }

    // Deleta um grupo
    public void deletarGrupo(String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Optional<Grupo> existente = grupoRepository.findById(id);
        if (existente.isPresent()) {
            Grupo original = existente.get();

            boolean Criador = original.getPerfilCriador().getId().equals(perfilLogado.getId());
            boolean Admin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!Criador && !Admin) {
                throw new SecurityException("Você não tem permissão para excluir este grupo.");
            }

            grupoRepository.deleteById(id);
        }
    }

    //Entra No Grupo
    public void entrarNoGrupo(String grupoId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        if (grupo.getParticipantes().contains(perfilLogado)) return;

        if (grupo.getParticipantes().size() >= grupo.getLimiteParticipantes()) {
            throw new IllegalStateException("Este grupo já atingiu o limite de participantes.");
        }

        grupo.getParticipantes().add(perfilLogado);
        grupoRepository.save(grupo);
    }

    //Lista Todos os Grupos em Paginas
    public Page<Grupo> listarTodosGruposPaginados(Pageable pageable) {
        return grupoRepository.findAll(pageable);
    }


}
