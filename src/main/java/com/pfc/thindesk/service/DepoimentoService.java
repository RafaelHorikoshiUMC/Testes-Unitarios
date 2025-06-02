package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.DepoimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DepoimentoService {

    @Autowired
    private DepoimentoRepository depoimentoRepository;
    @Autowired
    private PerfilService perfilService;

    // Cria um novo Depoimento
    public Depoimento criarDepoimento(Depoimento depoimento) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfil = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        LocalDateTime agora = LocalDateTime.now();
        depoimento.setCriador(perfil.getApelido());
        depoimento.setPerfilCriador(perfil);
        depoimento.setDataCriacao(agora);
        depoimento.setDataAtualizacao(agora);

        return depoimentoRepository.save(depoimento);
    }


    // Busca todos os Depoimentos
    public List<Depoimento> listarTodosDepoimentos() {
        return depoimentoRepository.findAll();
    }

    // Busca o Depoimento por ID
    public Optional<Depoimento> buscarDepoimentoPorId(String id) {
        return depoimentoRepository.findById(id);
    }

    // Atualizar um Depoimento
    public Depoimento atualizarDepoimento(String id, Depoimento novoDepoimento) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Optional<Depoimento> existente = depoimentoRepository.findById(id);
        if (existente.isPresent()) {
            Depoimento original = existente.get();
            if (!original.getPerfilCriador().getId().equals(perfilLogado.getId())) {
                throw new SecurityException("Você não tem permissão para editar este depoimento.");
            }

            original.setTexto(novoDepoimento.getTexto());
            original.setDataAtualizacao(LocalDateTime.now());
            return depoimentoRepository.save(original);
        }
        return null;
    }


    // Deleta um Depoimento
    public void deletarDepoimento(String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(email)
                .orElseThrow(() -> new RuntimeException("Perfil do usuário não encontrado"));

        Optional<Depoimento> existente = depoimentoRepository.findById(id);
        if (existente.isPresent()) {
            Depoimento original = existente.get();

            boolean Criador = original.getPerfilCriador().getId().equals(perfilLogado.getId());
            boolean Admin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!Criador && !Admin) {
                throw new SecurityException("Você não tem permissão para excluir este depoimento.");
            }
            depoimentoRepository.deleteById(id);
        }
    }

    //Lista Todos os Depoimentos em Paginas
    public Page<Depoimento> listarTodosDepoimentosPaginados(Pageable pageable) {
        return depoimentoRepository.findAll(pageable);
    }


}