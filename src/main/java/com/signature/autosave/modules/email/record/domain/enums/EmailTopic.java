package com.signature.autosave.modules.email.record.domain.enums;

public enum EmailTopic {
    TECH("Tecnologia"),
    GAMES("Jogos"),
    MUSIC("Musica"),
    MOVIES("Filmes"),
    SERIES("Series"),
    BOOKS("Livros"),
    VARIETY("Variedades"),;

    private String displayName;

    EmailTopic(String name) {
        this.displayName = this.name();
    }
}
