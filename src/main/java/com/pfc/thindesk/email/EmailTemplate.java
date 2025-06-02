package com.pfc.thindesk.email;

// Enum que representa os templates de e-mail disponíveis
public enum EmailTemplate {

    // Template para recuperação de senha (mapeia para o arquivo "recuperar-senha.html")
    RECUPERAR_SENHA("recuperar-senha");

    private final String name; // Nome do template (sem extensão .html)

    // Construtor do enum
    EmailTemplate(String name) {
        this.name = name;
    }

    // Retorna o nome do template
    public String getName() {
        return name;
    }
}

