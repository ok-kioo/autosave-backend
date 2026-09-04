package com.signature.autosave.modules.email.content.domain.enums;

public enum EmailTopic {
    TECH("Tecnologia"),
    GAMES("Jogos"),
    MUSIC("Musica"),
    MOVIES("Filmes"),
    SERIES("Series"),
    BOOKS("Livros"),
    VARIETY("Variedades"),;

    EmailTopic(String name) {
        String displayName = this.name();
    }
}
