package com.team901.CapstoneDesign.GPT.dto;

import lombok.Getter;
import lombok.Setter;

public class Content {

    @Getter
    @Setter
    private String content;

    public Content(String content) {
        this.content = content;
    }
}