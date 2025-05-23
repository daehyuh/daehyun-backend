package com.example.daehyunbackend.response;

import lombok.Data;

@Data
public class UserDataResponse {
    private int responseCode;
    private UserData userData;
}

