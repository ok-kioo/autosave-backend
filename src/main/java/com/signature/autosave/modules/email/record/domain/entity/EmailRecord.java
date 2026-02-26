package com.signature.autosave.modules.email.record.domain.entity;

import com.signature.autosave.modules.email.content.domain.entity.EmailContent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_record")
public class EmailRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    private int viewCount;

    @JoinColumn(name = "email_content_id", referencedColumnName = "id")
    @OneToOne
    private EmailContent emailContent;




}
