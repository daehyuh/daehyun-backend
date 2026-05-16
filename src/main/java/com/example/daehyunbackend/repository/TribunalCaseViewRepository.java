package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseView;
import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TribunalCaseViewRepository extends JpaRepository<TribunalCaseView, Long> {
    Optional<TribunalCaseView> findByTribunalCaseAndUser(TribunalCase tribunalCase, User user);

    long countByTribunalCase(TribunalCase tribunalCase);
}
