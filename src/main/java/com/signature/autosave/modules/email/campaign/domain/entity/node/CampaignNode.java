package com.signature.autosave.modules.email.campaign.domain.entity.node;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node("Campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignNode {

    @Id
    @Column(unique = true)
    private UUID id;

}
