package com.GitHubDataCollector.service;

import com.GitHubDataCollector.model.User;
import com.GitHubDataCollector.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    public static final String GREEN = "\033[32m";
    public static final String DEFAULT = "\033[0m";
    @Autowired
    UserRepository repository;

    public Iterable<User> all() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public User save(User user) throws InterruptedException {
        User newUser;
        try {
            Optional<User> optionalUser = repository.findByUsername(user.getUsername());
            if (optionalUser.isPresent()) {
                user.updateUser(optionalUser.get());
                newUser = repository.save(optionalUser.get());
                System.out.println(GREEN + "PostgreSQL | Update user | " + newUser.getUsername() + DEFAULT);
            }else {
                newUser = repository.save(user);
                System.out.println(GREEN + "PostgreSQL | Create new user | " + newUser.getUsername() + DEFAULT);
            }
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
        return newUser;
    }

    public void delete(User user) {
        repository.delete(user);
    }
}
