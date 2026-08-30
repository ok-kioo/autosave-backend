package com.signature.autosave.modules.user.domain.repository;

import com.signature.autosave.modules.user.domain.entity.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserNodeRepository extends Neo4jRepository<UserNode, UUID> {
    @Query("""
        MATCH (u:User {id: $userId})
        SET u.isActive = false,
            u.disabledAt = datetime()
        """)
    void softDelete(UUID userId);

    @Query("""
        MATCH (u:User {id: $userId})
        WHERE u.isActive = true
        RETURN u
        """)
    Optional<UserNode> findActiveById(UUID userId);

}
