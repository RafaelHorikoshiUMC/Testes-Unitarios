package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "mensagens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensagem {

    @Id
    private String id;
    private String remetenteId;
    private String destinatarioId;
    private String conteudo;
    private LocalDateTime dataHora;
    private String grupoId;

}
