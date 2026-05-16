package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalReplayMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TribunalReplayMessageRepository extends JpaRepository<TribunalReplayMessage, Long> {
    List<TribunalReplayMessage> findByTribunalCaseOrderBySequenceNoAsc(TribunalCase tribunalCase);

    void deleteByTribunalCase(TribunalCase tribunalCase);
}
