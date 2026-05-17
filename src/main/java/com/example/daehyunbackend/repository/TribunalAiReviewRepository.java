package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalAiReview;
import com.example.daehyunbackend.entity.TribunalCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TribunalAiReviewRepository extends JpaRepository<TribunalAiReview, Long> {
    Optional<TribunalAiReview> findByTribunalCase(TribunalCase tribunalCase);

    Optional<TribunalAiReview> findByTribunalCaseId(Long caseId);

    void deleteByTribunalCase(TribunalCase tribunalCase);
}
