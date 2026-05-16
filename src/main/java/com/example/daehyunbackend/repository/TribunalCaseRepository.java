package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TribunalCaseRepository extends JpaRepository<TribunalCase, Long> {
    @EntityGraph(attributePaths = "author")
    Page<TribunalCase> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
