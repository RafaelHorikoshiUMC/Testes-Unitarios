package com.pfc.thindesk.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sugestoesDeJogos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SugestaoDeJogo {

    @Id
    private String id;
    private String nomeDoJogoSugerido;
    private String descricaoOpcional;
    private String criador;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    @DBRef
    private Perfil perfilCriador;
}
