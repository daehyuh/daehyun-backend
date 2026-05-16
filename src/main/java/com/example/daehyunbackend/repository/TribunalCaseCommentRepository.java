package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TribunalCaseCommentRepository extends JpaRepository<TribunalCaseComment, Long> {
    @EntityGraph(attributePaths = {"author", "parent"})
    List<TribunalCaseComment> findByTribunalCaseOrderByCreatedAtAsc(TribunalCase tribunalCase);

    long countByTribunalCaseAndDeletedFalse(TribunalCase tribunalCase);
}
