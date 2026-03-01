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
    MATCH (c:Campaign {id: $campaignId})
    MATCH (c)<-[:ON]-(root:Comment)
    MATCH (authorRoot:User)-[:WROTE]->(root)
    
    OPTIONAL MATCH (reply:Comment)-[:REPLYING]->(root)
    OPTIONAL MATCH (authorReply:User)-[:WROTE]->(reply)
    
    WITH root, authorRoot,
         collect({
             comment: reply,
             author: authorReply
         }) AS replies
    
    RETURN root, authorRoot, replies
    """)
    List<CommentThreadProjection> findComments(UUID campaignId);

    Boolean commentWrittenByUser(UUID commentId, UUID userId);

    @Query("""
    MATCH (root:Comment {id: $commentId})
    OPTIONAL MATCH (child:Comment)-[:REPLYING*]->(root)
    DETACH DELETE root, child
    """)
    void deleteComment(UUID commentId);
}
