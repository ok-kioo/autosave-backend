package com.signature.autosave.modules.user.domain.entity.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.util.UUID;

@Node("User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserNode {

    @Id
    private UUID id;

    private String name;

    private LocalDateTime disabledAt;

    private boolean isActive = true;
}
