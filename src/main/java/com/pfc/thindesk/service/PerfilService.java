package com.pfc.thindesk.service;

import com.pfc.thindesk.PerfilMatchDTO;
import com.pfc.thindesk.entity.DecisaoMatch;
import com.pfc.thindesk.entity.Jogo;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.DecisaoMatchRepository;
import com.pfc.thindesk.repository.PerfilRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerfilService {
    @Autowired
    public PerfilRepository perfilRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private DecisaoMatchRepository decisaoMatchRepository;

    // Recupera o usuário atualmente autenticado
    public String getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                return usuarioOpt.get().getId(); // retorna o ID Mongo do usuário
            }
        }
        return null;
    }


    // Cria um novo perfil
    public Perfil criarPerfil(Perfil perfil) {
        String usuarioId = getUsuarioAutenticado();
        if (usuarioId == null) {
            throw new IllegalStateException("Usuário não autenticado. Não é possível criar um perfil.");
        }

        // Seta o usuarioId no perfil antes de salvar
        perfil.setUsuarioId(usuarioId);
        return perfilRepository.save(perfil);
    }

    // Lista apenas os perfis do usuário autenticado
    public List<Perfil> listarPerfisDoUsuario() {
        String usuarioAutenticado = getUsuarioAutenticado();
        if (usuarioAutenticado == null) {
            return new ArrayList<>(); // Nenhum perfil para usuário não autenticado
        }

        return perfilRepository.findAll().stream()
                .filter(perfil -> usuarioAutenticado.equals(perfil.getUsuarioId())) // Verifica null aqui
                .toList();
    }

    // Busca um perfil específico do usuário autenticado
    public Optional<Perfil> buscarPerfilPorId(String id) {
        String usuarioAutenticado = getUsuarioAutenticado();
        if (usuarioAutenticado == null) {
            return Optional.empty(); // Retorna vazio se o usuário não está autenticado
        }

        // Busca o perfil e filtra pelo usuarioId
        return perfilRepository.findById(id)
                .filter(perfil -> usuarioAutenticado.equals(perfil.getUsuarioId())); // Verifica null aqui
    }

    //Busca Perfis Por Ids
    public List<Perfil> buscarPerfisPorIds(Set<String> ids) {
        return perfilRepository.findAllById(ids);
    }

    //Listar Todos Perfis
    public List<Perfil> listarTodosPerfis() {
        return perfilRepository.findAll();
    }

    //Busca Perfil Por Id Sem Restricao
    public Optional<Perfil> buscarPerfilPorIdSemRestricao(String id) {
        return perfilRepository.findById(id);
    }


    // Atualiza apenas se o perfil pertencer ao usuário autenticado
    public Perfil atualizarPerfil(String id, Perfil perfilAtualizado) {
        String usuarioAutenticado = getUsuarioAutenticado();
        if (usuarioAutenticado == null) {
            throw new SecurityException("Usuário não autenticado. Operação não permitida.");
        }

        Perfil perfilExistente = perfilRepository.findById(id)
                .filter(perfil -> usuarioAutenticado.equals(perfil.getUsuarioId())) // Verifica null aqui
                .orElseThrow(() -> new SecurityException("Acesso negado ao perfil especificado!"));

        perfilAtualizado.setId(perfilExistente.getId()); // Garante que o ID original será mantido
        perfilAtualizado.setUsuarioId(perfilExistente.getUsuarioId()); // Mantém o usuário correto
        return perfilRepository.save(perfilAtualizado);
    }

    // Exclui apenas se o perfil pertencer ao usuário autenticado
    public void deletarPerfil(String id) {
        String usuarioAutenticado = getUsuarioAutenticado();
        if (usuarioAutenticado == null) {
            throw new SecurityException("Usuário não autenticado. Operação não permitida.");
        }

        Perfil perfilExistente = perfilRepository.findById(id)
                .filter(perfil -> usuarioAutenticado.equals(perfil.getUsuarioId())) // Verifica null aqui
                .orElseThrow(() -> new SecurityException("Acesso negado ao perfil especificado!"));

        perfilRepository.delete(perfilExistente);
    }

    // Busca perfis não reagidos e calcula a compatibilidade
    public List<PerfilMatchDTO> buscarPerfisNaoReagidos(Perfil perfilAtual) {
        List<Perfil> perfisNaoReagidos = obterPerfisNaoReagidos(perfilAtual);
        List<PerfilMatchDTO> resultados = calcularCompatibilidade(perfilAtual, perfisNaoReagidos);
        return resultados.stream()
                .sorted(Comparator.comparingInt(PerfilMatchDTO::scorePercentual).reversed())
                .toList();
    }

    //Obter perfis não reagidos
    public  List<Perfil> obterPerfisNaoReagidos(Perfil perfilAtual) {
        String perfilOrigemId = perfilAtual.getId();

        Set<String> idsJaReagidos = decisaoMatchRepository.findByPerfilOrigemId(perfilOrigemId).stream()
                .map(DecisaoMatch::getPerfilAlvoId)
                .collect(Collectors.toSet());

        return perfilRepository.findAll().stream()
                .filter(outro -> !outro.getId().equals(perfilOrigemId))
                .filter(outro -> !idsJaReagidos.contains(outro.getId()))
                .toList();
    }

    // Algoritmo de compatibilidade
    public  List<PerfilMatchDTO> calcularCompatibilidade(Perfil perfilAtual, List<Perfil> outrosPerfis) {
        List<PerfilMatchDTO> resultados = new ArrayList<>();

        for (Perfil outroPerfil : outrosPerfis) {
            int atributosEmComum = 0;
            int totalAtributos = 60;

            if (camposIguais(perfilAtual.getPlataforma(), outroPerfil.getPlataforma())) atributosEmComum += 15;
            if (camposIguais(perfilAtual.getComunicacao(), outroPerfil.getComunicacao())) atributosEmComum += 10;
            if (camposIguais(perfilAtual.getPeriodoOnline(), outroPerfil.getPeriodoOnline())) atributosEmComum += 12;
            if (camposIguais(perfilAtual.getEstiloDeJogo(), outroPerfil.getEstiloDeJogo())) atributosEmComum += 8;
            if (camposIguais(perfilAtual.getGeneroPreferidoPrincipal(), outroPerfil.getGeneroPreferidoPrincipal())) atributosEmComum += 6;

            String nomeJogoAtual = perfilAtual.getJogoPreferido() != null ? perfilAtual.getJogoPreferido().getNome() : null;
            String nomeJogoOutro = outroPerfil.getJogoPreferido() != null ? outroPerfil.getJogoPreferido().getNome() : null;
            if (camposIguais(nomeJogoAtual, nomeJogoOutro)) atributosEmComum += 5;

            if (camposIguais(perfilAtual.getGeneroPreferidoSecundario(), outroPerfil.getGeneroPreferidoSecundario())) atributosEmComum += 3;
            if (camposIguais(perfilAtual.getEstadoCivil(), outroPerfil.getEstadoCivil())) atributosEmComum += 1;

            int porcentagem = (int) ((atributosEmComum / (double) totalAtributos) * 100);
            resultados.add(new PerfilMatchDTO(outroPerfil, porcentagem));
        }

        return resultados;
    }


    // Metodo auxiliar para comparação segura de strings
    private boolean camposIguais(String a, String b) {
        return (a == null && b == null) || (a != null && a.equalsIgnoreCase(b));
    }

    //Busca o Perfil Do Usuario Logado
    public Optional<Perfil> buscarPerfilDoUsuarioLogado(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        String usuarioId = usuarioOpt.get().getId();  // pega o ID do usuário
        return perfilRepository.findByUsuarioId(usuarioId);
    }

    //Busca Pelo  Usuario
    public Optional<Perfil> buscarPorUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return Optional.empty();
        }
        return perfilRepository.findByUsuarioId(usuario.getId());
    }

}