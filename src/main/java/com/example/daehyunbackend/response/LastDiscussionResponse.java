package com.example.daehyunbackend.response;
import com.example.daehyunbackend.service.CommentData;
import lombok.Data;

import java.util.List;

@Data
public class LastDiscussionResponse {
    private int code;
    private List<CommentData> commentData;
}

