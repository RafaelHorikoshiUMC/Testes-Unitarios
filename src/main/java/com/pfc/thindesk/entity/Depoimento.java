package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;


import java.time.LocalDateTime;

@Document(collection = "depoimentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Depoimento {

    @Id
    private String id;
    private String texto;
    private String criador;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    @DBRef
    private Perfil perfilCriador;
}

