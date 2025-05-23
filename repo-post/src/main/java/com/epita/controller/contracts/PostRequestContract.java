package com.epita.controller.contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;


import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostRequestContract {

    private String text;

    public String mediaId;

    private UUID repostId;

    private UUID replyId;
}