package com.team901.CapstoneDesign.GPT.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Content {

    private String content;

    public Content(String content) {
        this.content = content;
    }
}