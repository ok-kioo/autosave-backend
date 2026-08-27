package com.signature.autosave.modules.email.campaign.domain.entity.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node("Campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignNode {

    @Id
    private UUID id;

}
