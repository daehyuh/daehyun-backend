package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.MobileAuthTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface MobileAuthTicketRepository extends JpaRepository<MobileAuthTicket, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MobileAuthTicket> findByTicketHash(String ticketHash);
}
