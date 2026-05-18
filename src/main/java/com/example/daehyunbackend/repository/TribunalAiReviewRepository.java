package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalAiReview;
import com.example.daehyunbackend.entity.TribunalAiReviewStatus;
import com.example.daehyunbackend.entity.TribunalCase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TribunalAiReviewRepository extends JpaRepository<TribunalAiReview, Long> {
    Optional<TribunalAiReview> findByTribunalCase(TribunalCase tribunalCase);

    Optional<TribunalAiReview> findByTribunalCaseId(Long caseId);

    @Query("""
            select review
            from TribunalAiReview review
            where review.status = :status
              and (review.nextRetryAt is null or review.nextRetryAt <= :now)
              and coalesce(review.retryCount, 0) < :maxAttempts
            order by review.nextRetryAt asc, review.id asc
            """)
    List<TribunalAiReview> findRetryableFailures(
            @Param("status") TribunalAiReviewStatus status,
            @Param("now") LocalDateTime now,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable
    );

    void deleteByTribunalCase(TribunalCase tribunalCase);
}
