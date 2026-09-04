package com.signature.autosave.modules.email.campaign.domain.entity.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.util.UUID;

@Node("Comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentNode {

    @Id
    @GeneratedValue(value = GeneratedValue.UUIDGenerator.class)
    private UUID id;

    private String text;

    private LocalDateTime dateTime;

    private boolean isActive = true;

    private LocalDateTime disabledAt;
}
