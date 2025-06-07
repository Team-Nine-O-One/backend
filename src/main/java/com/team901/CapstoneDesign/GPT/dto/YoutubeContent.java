package com.team901.CapstoneDesign.GPT.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YoutubeContent {

    private String title;


    private List<String> ingredients;

    public YoutubeContent(String title, List<String> ingredients) {
        this.title = title;
        this.ingredients = ingredients;
    }

}
