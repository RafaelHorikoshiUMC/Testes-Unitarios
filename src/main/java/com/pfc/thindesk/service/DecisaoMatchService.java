package com.pfc.thindesk.service;

import com.pfc.thindesk.MatchDTO;
import com.pfc.thindesk.entity.DecisaoMatch;
import com.pfc.thindesk.entity.Depoimento;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.repository.DecisaoMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DecisaoMatchService {

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private DecisaoMatchRepository decisaoMatchRepository; // repositório Mongo ou JPA

    //Salva a Decisao
    public void salvarDecisao(String perfilOrigemId, String perfilAlvoId, boolean deuMatch) {
        DecisaoMatch decisao = new DecisaoMatch();
        decisao.setPerfilOrigemId(perfilOrigemId);
        decisao.setPerfilAlvoId(perfilAlvoId);
        decisao.setDeuMatch(deuMatch);
        decisaoMatchRepository.save(decisao);
    }

    // Busca todos as Decisões
    public List<DecisaoMatch> listarTodasDecisaoMatch() {
        return decisaoMatchRepository.findAll();
    }

    //Verificar Match
    public MatchDTO verificarMatch(String perfilAId, String perfilBId) {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setPerfilAId(perfilAId);
        matchDTO.setPerfilBId(perfilBId);

        // Busca perfis pelo ID
        Optional<Perfil> perfilAOpt = perfilService.buscarPerfilPorIdSemRestricao(perfilAId);
        Optional<Perfil> perfilBOpt = perfilService.buscarPerfilPorIdSemRestricao(perfilBId);

        // Seta os apelidos se encontrados
        perfilAOpt.ifPresent(perfilA -> matchDTO.setApelidoPerfilA(perfilA.getApelido()));
        perfilBOpt.ifPresent(perfilB -> matchDTO.setApelidoPerfilB(perfilB.getApelido()));

        // Verifica se ambos deram "sim"
        boolean aDeuSim = decisaoMatchRepository
                .existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(perfilAId, perfilBId, true);
        boolean bDeuSim = decisaoMatchRepository
                .existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(perfilBId, perfilAId, true);

        matchDTO.setHouveMatch(aDeuSim && bDeuSim);
        return matchDTO;
    }

    //Lista Matches Comigo
    public List<DecisaoMatch> listarMatchesComigo(String meuPerfilId) {
        // Match mútuo: alguém me curtiu (perfilAlvoId == meu ID) e eu curti de volta (existe Decisao com deuMatch = true nos dois sentidos)
        List<DecisaoMatch> quemMeCurtiu = decisaoMatchRepository.findByPerfilAlvoIdAndDeuMatch(meuPerfilId, true);

        return quemMeCurtiu.stream()
                .filter(decisao -> decisaoMatchRepository
                        .existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(
                                meuPerfilId,
                                decisao.getPerfilOrigemId(),
                                true))
                .toList();
    }

    //Lista Quem Me Curtiu Sem Resposta
    public List<Perfil> listarQuemMeCurtiuSemResposta(String meuPerfilId) {
        // 1. Busca quem CURTIU você (ou seja, perfilAlvo == você, deuMatch == true)
        List<DecisaoMatch> curtidasRecebidas = decisaoMatchRepository.findByPerfilAlvoIdAndDeuMatch(meuPerfilId, true);

        // 2. Filtra os que você ainda NÃO respondeu
        return curtidasRecebidas.stream()
                .filter(decisao -> !decisaoMatchRepository.existsByPerfilOrigemIdAndPerfilAlvoId(
                        meuPerfilId, decisao.getPerfilOrigemId()))
                .map(decisao -> perfilService.buscarPerfilPorIdSemRestricao(decisao.getPerfilOrigemId()))

                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
