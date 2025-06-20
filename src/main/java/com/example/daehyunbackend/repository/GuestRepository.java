package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Guest;
import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    List<Guest> findByNickName(String nickName);
}