package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);

    @Query(value = """
            select distinct u
            from User u
            left join Account a on a.user = u
            left join Record r on r.account = a
            where lower(coalesce(u.email, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(u.name, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(u.providerId, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(r.NICKNAME, '')) like lower(concat('%', :query, '%'))
            """,
            countQuery = """
            select count(distinct u.id)
            from User u
            left join Account a on a.user = u
            left join Record r on r.account = a
            where lower(coalesce(u.email, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(u.name, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(u.providerId, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(r.NICKNAME, '')) like lower(concat('%', :query, '%'))
            """)
    Page<User> searchForAdmin(@Param("query") String query, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
