package com.GitHubDataCollectorV2.repo;

import com.GitHubDataCollectorV2.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Long> {
    Optional<User> findByUsername(String username);
}
