package com.team901.CapstoneDesign.GPT;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Message {
    private String role;
    private String content;
}

