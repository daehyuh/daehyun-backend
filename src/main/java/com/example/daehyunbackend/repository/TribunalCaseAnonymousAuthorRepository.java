package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseAnonymousAuthor;
import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TribunalCaseAnonymousAuthorRepository extends JpaRepository<TribunalCaseAnonymousAuthor, Long> {
    Optional<TribunalCaseAnonymousAuthor> findByTribunalCaseAndUser(TribunalCase tribunalCase, User user);

    int countByTribunalCase(TribunalCase tribunalCase);
}
