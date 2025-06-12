package com.example.daehyunbackend.entity;

import com.example.daehyunbackend.response.UserData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordid;
    private int win_count;
    private int lose_count;
    private int rankpoint;
    private int fame;
    private String introduce;
    private int frame;
    private Long current_collection;
    private int current_nametag;
    private String current_skin;
    @Setter
    private int nickname_color;
    private Long current_collection2;
    private int rankpoint2;
    private String current_gem;
    private Long current_collection3;
    private int EXPERIENCE;
    private int EXPERIENCE2;
    @Setter
    private String NICKNAME;
    private Long ID;
    private int guild_id;
    private int guild_point;
    private String guild_name;
    private int guild_level;
    private String guild_initial;
    private int guild_initial_color;
    @Setter
    private int guild_initial_background_color;
    private String gem;

    @Setter
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "AcoountId", referencedColumnName = "id")
    private Account account;

    @Setter
    private LocalDate date;


    public static Record fromEntity(UserData userData){
        return Record.builder()
                // account(userData.getAccount())
                .win_count(userData.getWin_count())
                .lose_count(userData.getLose_count())
                .rankpoint(userData.getRankpoint2())
                .fame(userData.getFame())
                .introduce(userData.getIntroduce())
                .frame(userData.getFrame())
                .current_collection(userData.getCurrent_collection())
                .current_nametag(userData.getCurrent_nametag())
                .current_skin(userData.getCurrent_skin())
                .nickname_color(userData.getNickname_color())
                .current_collection2(userData.getCurrent_collection2())
                .rankpoint2(userData.getRankpoint2())
                .current_gem(userData.getCurrent_gem())
                .current_collection3(userData.getCurrent_collection3())
                .EXPERIENCE(userData.getEXPERIENCE())
                .EXPERIENCE2(userData.getEXPERIENCE2())
                .NICKNAME(userData.getNICKNAME())
                .ID(Long.valueOf(userData.getID())) // 강제 형변환
                .guild_id(userData.getGuild_id())
                .guild_point(userData.getGuild_point())
                .guild_name(userData.getGuild_name())
                .guild_level(userData.getGuild_level())
                .guild_initial(userData.getGuild_initial())
                .guild_initial_color(userData.getGuild_initial_color())
                .guild_initial_background_color(userData.getGuild_initial_background_color())
                .gem(userData.getGem())
                .date(LocalDate.now())
                .build();
    }


}