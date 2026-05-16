package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TribunalCaseCommentRepository extends JpaRepository<TribunalCaseComment, Long> {
    @EntityGraph(attributePaths = {"author", "parent"})
    List<TribunalCaseComment> findByTribunalCaseOrderByCreatedAtAsc(TribunalCase tribunalCase);

    List<TribunalCaseComment> findByTribunalCaseOrderByIdDesc(TribunalCase tribunalCase);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TribunalCaseComment c set c.parent = null where c.tribunalCase = :tribunalCase")
    void detachParentsByTribunalCase(TribunalCase tribunalCase);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from TribunalCaseComment c where c.tribunalCase = :tribunalCase")
    void deleteByTribunalCase(TribunalCase tribunalCase);

    long countByTribunalCaseAndDeletedFalse(TribunalCase tribunalCase);
}
