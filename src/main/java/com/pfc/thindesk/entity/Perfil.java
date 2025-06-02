package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "perfis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    private String id;
    private String apelido;
    private String nascimento;
    private String generoPreferidoPrincipal;
    private String generoPreferidoSecundario;
    private String comunicacao;
    private String plataforma;
    private String periodoOnline;
    private String estiloDeJogo;
    private String descricao;
    private String estadoCivil;
    private String usuarioId;
    private String fotoUrl;
    @DBRef
    private Jogo jogoPreferido;

}