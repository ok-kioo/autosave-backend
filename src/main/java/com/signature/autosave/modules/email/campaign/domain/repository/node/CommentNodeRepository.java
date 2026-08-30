package com.signature.autosave.modules.email.campaign.domain.repository.node;

import com.signature.autosave.modules.email.campaign.domain.entity.node.CommentNode;
import com.signature.autosave.modules.email.campaign.dto.node.CommentThreadProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentNodeRepository extends Neo4jRepository<CommentNode, UUID> {
    @Query("""
    MATCH (u:User {id: $userId})
    MATCH (c:Campaign {id: $campaignId})
    CREATE (cm:Comment {
        id: $commentId,
        text: $text,
        at: datetime()
    })
    CREATE (u)-[:WROTE]->(cm)
    CREATE (cm)-[:ON]->(c)
    RETURN cm AS root, collect(reply) AS replies
    """)
    CommentThreadProjection registerCampaignComment(UUID userId, UUID campaignId, UUID commentId, String text);

    @Query("""
    MATCH (u:User {id: $userId})
    MATCH (parent:Comment {id: $parentCommentId})
    CREATE (cm:Comment {
        id: $commentId,
        text: $text,
        at: datetime()
    })
    CREATE (u)-[:WROTE]->(cm)
    CREATE (cm)-[:REPLYING]->(parent)
    RETURN cm AS root, collect(reply) AS replies
    """)
    CommentThreadProjection replyComment(UUID userId, UUID parentCommentId, UUID commentId, String text);

    @Query("""
    MATCH (c:Campaign {id: $campaignId})<-[:COMMENTED]-(root:Comment)
    MATCH (rootAuthor:User)-[:COMMENTED]->(root)

    OPTIONAL MATCH (root)<-[:REPLY_TO]-(reply:Comment)
    OPTIONAL MATCH (replyAuthor:User)-[:COMMENTED]->(reply)

    WITH root, rootAuthor,
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
    RETURN count(c) > 0
    """)
    Boolean commentWrittenByUser(UUID commentId, UUID userId);

    @Query("""
    MATCH (root:Comment {id: $commentId})
    OPTIONAL MATCH (child:Comment)-[:REPLYING*]->(root)
    DETACH DELETE root, child
    """)
    void deleteComment(UUID commentId);
}
