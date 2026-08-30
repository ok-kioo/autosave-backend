package com.signature.autosave.modules.email.campaign.domain.repository.node;

import com.signature.autosave.modules.email.campaign.domain.entity.node.CommentNode;
import com.signature.autosave.modules.email.campaign.dto.node.CommentThreadProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentNodeRepository extends Neo4jRepository<CommentNode, UUID> {

    @Query("""
        MATCH (u:User {id: $userId})
        MATCH (c:Campaign {id: $campaignId})

        WHERE u.isActive = true
          AND c.isActive = true

        CREATE (cm:Comment {
            id: $commentId,
            text: $text,
            dateTime: datetime(),
            isActive: true
        })

        CREATE (u)-[:WROTE]->(cm)
        CREATE (cm)-[:ON]->(c)

        RETURN cm AS root,
               u AS authorRoot,
               [] AS replies
        """)
    CommentThreadProjection registerCampaignComment(
            UUID userId,
            UUID campaignId,
            UUID commentId,
            String text
    );

    @Query("""
        MATCH (u:User {id: $userId})
        MATCH (parent:Comment {id: $parentCommentId})

        WHERE u.isActive = true
          AND parent.isActive = true

        CREATE (cm:Comment {
            id: $commentId,
            text: $text,
            dateTime: datetime(),
            isActive: true
        })

        CREATE (u)-[:WROTE]->(cm)
        CREATE (cm)-[:REPLYING]->(parent)

        RETURN cm AS root,
               u AS authorRoot,
               [] AS replies
        """)
    CommentThreadProjection replyComment(
            UUID userId,
            UUID parentCommentId,
            UUID commentId,
            String text
    );

    @Query("""
        MATCH (comment:Comment {id: $commentId})

        WHERE comment.isActive = true

        RETURN comment
        """)
    Optional<CommentNode> findActiveById(UUID commentId);

    @Query("""
        MATCH (c:Campaign {id: $campaignId})

        WHERE c.isActive = true

        MATCH (root:Comment)-[:ON]->(c)

        WHERE root.isActive = true

        MATCH (rootAuthor:User)-[:WROTE]->(root)

        WHERE rootAuthor.isActive = true

        OPTIONAL MATCH (reply:Comment)-[:REPLYING]->(root)

        WHERE reply.isActive = true

        OPTIONAL MATCH (replyAuthor:User)-[:WROTE]->(reply)

        WHERE replyAuthor.isActive = true

        WITH root,
             rootAuthor,
             collect({
                 comment: reply,
                 author: replyAuthor
             }) AS replies

        RETURN root,
               rootAuthor,
               replies

        ORDER BY root.dateTime DESC

        SKIP $skip
        LIMIT $limit
        """)
    List<CommentThreadProjection> findComments(
            UUID campaignId,
            long skip,
            long limit
    );

    @Query("""
        MATCH (u:User {id: $userId})-[:WROTE]->(c:Comment {id: $commentId})

        WHERE u.isActive = true
          AND c.isActive = true

        RETURN count(c) > 0
        """)
    Boolean commentWrittenByUser(
            UUID commentId,
            UUID userId
    );


    @Query("""
        MATCH (root:Comment {id: $commentId})

        WHERE root.isActive = true

        OPTIONAL MATCH (reply:Comment)-[:REPLYING*1..]->(root)

        WITH collect(root) + collect(reply) AS comments

        UNWIND comments AS comment

        WITH DISTINCT comment

        WHERE comment IS NOT NULL
          AND comment.isActive = true

        SET comment.isActive = false,
            comment.disabledAt = datetime()
        """)
    void softDelete(UUID commentId);


    @Query("""
        MATCH (campaign:Campaign {id: $campaignId})

        OPTIONAL MATCH (root:Comment)-[:ON]->(campaign)

        OPTIONAL MATCH (reply:Comment)-[:REPLYING*1..]->(root)

        WITH collect(root) + collect(reply) AS comments

        UNWIND comments AS comment

        WITH DISTINCT comment

        WHERE comment IS NOT NULL
          AND comment.isActive = true

        SET comment.isActive = false,
            comment.disabledAt = datetime()
        """)
    void softDeleteCampaignComments(UUID campaignId);
}
