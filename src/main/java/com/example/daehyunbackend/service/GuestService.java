package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Guest;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.repository.GuestRepository;
import com.example.daehyunbackend.repository.UserRepository;
import com.example.daehyunbackend.response.LastDiscussionResponse;
import com.example.daehyunbackend.response.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuestService {
    private final GuestRepository guestRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public void saveGuest(String nickname, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        List<Guest> existingGuests = guestRepository.findByNickName(nickname);
        if (!existingGuests.isEmpty()) {
            String newCode = String.format("%04d", (int) (Math.random() * 10000));
            for (Guest existingGuest : existingGuests) {
                if (existingGuest.getAccountId() != null) {
                    continue; // 이미 accountId가 있는 경우는 건너뜀
                }
                existingGuest.setCode(newCode);
                guestRepository.save(existingGuest);
            }
        } else {
            Guest guest = new Guest();
            guest.setUser(user.get());
            guest.setNickName(nickname);
            guest.setCreatedAt(LocalDateTime.now());
            String code = String.format("%04d", (int) (Math.random() * 10000));
            guest.setCode(code);
            guestRepository.save(guest);
        }
    }

    public void getLastDiscussion() {
        LocalDateTime now = LocalDateTime.now();

        List<Guest> guests = guestRepository.findAll();
        final String url = "https://mafia42.com/comment/show-lastDiscussion";

        // 요청 바디 만들기
        Map<String, Object> comment = new HashMap<>();
        comment.put("article_id", "349313");
        comment.put("value", 0);

        Map<String, Object> body = new HashMap<>();
        body.put("comment", comment);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 본문 설정
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // RestTemplate 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(factory);

        List<CommentData> CommentDatas = restTemplate.postForObject(url, entity, LastDiscussionResponse.class).getCommentData();
        for (Guest guest : guests) {
            if (guest.getCreatedAt().isBefore(now.minusMinutes(5))) {
                guestRepository.delete(guest);
                continue;
            }

            for (CommentData commentData : CommentDatas) {
                if (guest.getNickName().equals(commentData.getNickname())) {

                    if (guest.getCode().equals(commentData.getComment_content())){

                        guest.setAccountId(commentData.getUser_id());
                        guestRepository.save(guest);
                        System.out.println("저장");

                        Account account = accountRepository.findByAccountId(commentData.getUser_id());
                        System.out.println("account = " + account);
                        if (account != null) {
                            guestRepository.delete(guest);
                            continue;
                        } else {
                            Account ac = Account.builder()
                                    .user(guest.getUser())
                                    .accountId(commentData.getUser_id())
                                    .secretKey(null)
                                    .createdAt(LocalDateTime.now()).build();

                            accountRepository.save(ac);

                        }
                    }
                }
            }
        }

    }

}