package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "grupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grupo {

    @Id
    private String id;
    private String criador;
    private int limiteParticipantes;
    @DBRef
    private List<Perfil> participantes = new ArrayList<>();
    private String objetivo;
    private String horario;
    @DBRef
    private Perfil perfilCriador;
    @DBRef
    private Jogo jogo;
}
