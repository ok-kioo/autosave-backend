package com.signature.autosave.modules.email.emailcontent.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_content")
public class EmailContent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    private String title;

    private String source;

    private String destination;

    @NotBlank
    private String content;

    @NotBlank
    private String body;
}
