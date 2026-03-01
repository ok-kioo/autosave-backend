package com.signature.autosave.modules.user.domain.repository;

import com.signature.autosave.modules.user.domain.entity.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.UUID;

public interface UserNodeRepository extends Neo4jRepository<UserNode, UUID> {

}
