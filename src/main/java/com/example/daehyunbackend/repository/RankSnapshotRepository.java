package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.RankSnapshot;
import com.example.daehyunbackend.entity.RankType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RankSnapshotRepository extends JpaRepository<RankSnapshot, Long> {
    Page<RankSnapshot> findByRankTypeAndRankingDateOrderByRankNoAsc(
            RankType rankType,
            LocalDate rankingDate,
            Pageable pageable
    );

    List<RankSnapshot> findByRankTypeAndRankingDateOrderByRankNoAsc(RankType rankType, LocalDate rankingDate);

    Optional<RankSnapshot> findTopByRankTypeAndRankingDateOrderByCreatedAtDesc(
            RankType rankType,
            LocalDate rankingDate
    );

    void deleteByRankTypeAndRankingDate(RankType rankType, LocalDate rankingDate);
}
