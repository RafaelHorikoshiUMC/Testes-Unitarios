package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.DecisaoMatch;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface DecisaoMatchRepository extends MongoRepository<DecisaoMatch, String> {


   // Verifica se existe uma decisão de match entre dois perfis,onde a decisão foi positiva
    boolean existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(String perfilOrigemId, String perfilAlvoId, boolean deuMatch);

    //Busca as decisões em que o perfil foi o alvo da decisão e o resultado do match
    List<DecisaoMatch> findByPerfilAlvoIdAndDeuMatch(String perfilAlvoId, boolean deuMatch);

    //Verifica se já existe alguma decisão registrada entre dois perfis
    boolean existsByPerfilOrigemIdAndPerfilAlvoId(String perfilOrigemId, String perfilAlvoId);

    // Busca as decisões feitas por um perfil
    List<DecisaoMatch> findByPerfilOrigemId(String perfilOrigemId);

}

