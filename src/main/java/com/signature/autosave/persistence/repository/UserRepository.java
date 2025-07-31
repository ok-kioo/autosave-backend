package com.signature.autosave.persistence.repository;

import com.signature.autosave.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
}
