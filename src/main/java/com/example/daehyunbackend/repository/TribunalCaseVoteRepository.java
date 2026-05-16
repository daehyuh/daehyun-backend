package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseVote;
import com.example.daehyunbackend.entity.TribunalVerdict;
import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TribunalCaseVoteRepository extends JpaRepository<TribunalCaseVote, Long> {
    Optional<TribunalCaseVote> findByTribunalCaseAndVoter(TribunalCase tribunalCase, User voter);

    long countByTribunalCaseAndVerdict(TribunalCase tribunalCase, TribunalVerdict verdict);
}
