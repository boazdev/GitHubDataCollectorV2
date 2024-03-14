package com.GitHubDataCollectorV2.repo;

import com.GitHubDataCollectorV2.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "select percentile from percentiles where code_lines_min > ?1 order by code_lines_min limit 1", nativeQuery = true)
    Integer getLineOfCodePercentile(Integer linesOfCode);

    @Query(value = "select percentile from percentiles where commits_min > ?1 order by commits_min limit 1", nativeQuery = true)
    Integer getCommitsPercentile(Integer commits);

    @Query(value = "select percentile from percentiles where public_repos_min > ?1 order by public_repos_min limit 1", nativeQuery = true)
    Integer getRepositoriesPercentile(Integer repositories);
}
