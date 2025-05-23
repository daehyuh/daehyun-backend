package com.example.daehyunbackend.unit;

import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        System.out.println("username: " + username);
        final User user = this.userRepository
                .findById(Long.valueOf(username))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid authentication!"));

        return new CustomUserDetails(user);
    }
}
