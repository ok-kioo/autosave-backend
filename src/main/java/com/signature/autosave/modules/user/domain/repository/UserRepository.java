package com.signature.autosave.modules.user.domain.repository;

import com.signature.autosave.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByName(String name);
    List<User> findUsersByNameIn(List<String> names);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
}
