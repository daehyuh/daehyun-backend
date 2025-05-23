package com.example.daehyunbackend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserData {
    private int win_count;
    private int lose_count;
    private int rankpoint;
    private int fame;
    private String introduce;
    private int frame;
    private String current_collection;
    private int current_nametag;
    private String current_skin;
    private int nickname_color;
    private int current_collection2;
    private int rankpoint2;
    private String current_gem;
    private int current_collection3;
    private int EXPERIENCE;
    private int EXPERIENCE2;
    @JsonProperty("NICKNAME")
    private String NICKNAME;
    @JsonProperty("ID")
    private String ID;
    private int guild_id;
    private int guild_point;
    private String guild_name;
    private int guild_level;
    private String guild_initial;
    private int guild_initial_color;
    private int guild_initial_background_color;
    private String gem;
}
