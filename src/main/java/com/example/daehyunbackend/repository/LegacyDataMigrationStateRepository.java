package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.LegacyDataMigrationState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegacyDataMigrationStateRepository extends JpaRepository<LegacyDataMigrationState, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from LegacyDataMigrationState s where s.migrationName = :migrationName")
    Optional<LegacyDataMigrationState> findByMigrationNameForUpdate(@Param("migrationName") String migrationName);
}
