package com.example.daehyunbackend.service;

import com.example.daehyunbackend.dto.UserDto;
import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());

        return userRepository.save(existingUser);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Long count() {
        return userRepository.count();
    }

    public Long countToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        return userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

}
