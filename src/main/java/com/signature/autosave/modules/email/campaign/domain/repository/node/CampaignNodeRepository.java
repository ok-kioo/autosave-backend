package com.signature.autosave.modules.email.campaign.domain.repository.node;

import com.signature.autosave.modules.email.campaign.domain.entity.node.CampaignNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignNodeRepository
        extends Neo4jRepository<CampaignNode, UUID> {

    @Query("""
        MATCH (c:Campaign {id: $campaignId})
        WHERE c.isActive = true

        RETURN c
        """)
    Optional<CampaignNode> findActiveById(UUID campaignId);

    @Query("""
        MATCH (c:Campaign {id: $campaignId})
        WHERE c.isActive = true

        SET c.isActive = false,
            c.disabledAt = datetime()
        """)
    void softDelete(UUID campaignId);

    @Query("""
        MATCH (u:User {id: $userId})
        MATCH (c:Campaign {id: $campaignId})

        WHERE u.isActive = true
          AND c.isActive = true

        MERGE (u)-[r:VIEWED]->(c)

        SET r.at = datetime()
        """)
    void registerView(
            UUID userId,
            UUID campaignId
    );

    @Query("""
        MATCH (:User)-[r:VIEWED]->(c:Campaign {id: $campaignId})

        WHERE c.isActive = true

        RETURN count(r)
        """)
    Long countViews(UUID campaignId);

    @Query("""
        MATCH (u:User {id: $userId})
        MATCH (c:Campaign {id: $campaignId})

        WHERE u.isActive = true
          AND c.isActive = true

        OPTIONAL MATCH (u)-[r:LIKED]->(c)

        WITH u, c, r

        FOREACH (_ IN CASE
            WHEN r IS NULL THEN [1]
            ELSE []
        END |
            CREATE (u)-[:LIKED {
                at: datetime()
            }]->(c)
        )

        FOREACH (_ IN CASE
            WHEN r IS NOT NULL THEN [1]
            ELSE []
        END |
            DELETE r
        )

        RETURN r IS NULL AS liked
        """)
    Boolean toggleLike(
            UUID userId,
            UUID campaignId
    );

    @Query("""
        MATCH (:User)-[r:LIKED]->(c:Campaign {id: $campaignId})

        WHERE c.isActive = true

        RETURN count(r)
        """)
    Long countLikes(UUID campaignId);
}
