package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.TribunalCaseComment;
import com.example.daehyunbackend.entity.TribunalCommentLike;
import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TribunalCommentLikeRepository extends JpaRepository<TribunalCommentLike, Long> {
    Optional<TribunalCommentLike> findByCommentAndUser(TribunalCaseComment comment, User user);

    long countByComment(TribunalCaseComment comment);
}
