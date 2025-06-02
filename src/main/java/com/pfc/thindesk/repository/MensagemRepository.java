package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.Mensagem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MensagemRepository extends MongoRepository<Mensagem, String> {

    //Busca todas as mensagens entre os perfis
    List<Mensagem> findByRemetenteIdAndDestinatarioIdOrRemetenteIdAndDestinatarioId(
            String remetenteId1, String destinatarioId1, String remetenteId2, String destinatarioId2
    );

    //Busca todas as mensagens de um grupo
    List<Mensagem> findByGrupoIdOrderByDataHora(String grupoId);
}

