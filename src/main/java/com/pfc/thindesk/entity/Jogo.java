package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jogos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jogo {

    @Id
    private String id;
    private String nome;
    private String categoria;
    private String plataforma;
    private String desenvolvedora;
    private String anoDeLancamento;
}