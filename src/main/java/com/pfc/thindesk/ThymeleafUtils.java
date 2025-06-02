package com.pfc.thindesk;

import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component("utils")
public class ThymeleafUtils {

    @Autowired
    private PerfilRepository perfilRepository;

    public String apelidosDosParticipantes(List<Perfil> participantes) {
        if (participantes == null) return "";
        return participantes.stream()
                .map(Perfil::getApelido)
                .collect(Collectors.joining(", "));
    }

    public String nomeDoPerfil(String id) {
        return perfilRepository.findById(id)
                .map(Perfil::getApelido)
                .orElse("Desconhecido");
    }
}

