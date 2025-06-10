package com.team901.CapstoneDesign.youtube;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.team901.CapstoneDesign.GPT.dto.YoutubeContent;
import com.team901.CapstoneDesign.GPT.service.GPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeDataService {
    private final GPTService gptService;


    @Value("${youtube.api.key}")
    private String apiKey;


    public Video getVideoInfo(String videoId) throws IOException {
        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                request -> {})
                .setApplicationName("your-app-name")
                .build();

        YouTube.Videos.List request = youtube.videos()
                .list("snippet,statistics")
                .setId(videoId)
                .setKey(apiKey);

        VideoListResponse response = request.execute();
        List<Video> videos = response.getItems();
        return videos.isEmpty() ? null : videos.get(0);
    }

    public YoutubeContent getYoutubeVideoInfo(String url) throws Exception {
        String videoId = extractVideoId(url);
        Video video = getVideoInfo(videoId);

        String title = video.getSnippet().getTitle();
        String description = video.getSnippet().getDescription();

        List<String> ingredients =youtube(description);

        return new YoutubeContent(title, ingredients);
    }

    public String extractVideoId(String url) {
        if (url.contains("watch?v=")) {
            return url.substring(url.indexOf("v=") + 2);
        } else if (url.contains("shorts/")) {
            return url.substring(url.indexOf("shorts/") + 7);
        } else {
            throw new IllegalArgumentException("올바른 유튜브 URL이 아닙니다.");
        }
    }


    public List<String> youtube(String description) {
        String result=gptService.getChatResponse(description+
                "\n -----------\n  재료만 양과 단위를 포함해서 ,로 구분해서 알려줘 단위가 없으면 한개, 한스푼 이런식으로 넣어줘");

        List<String> ingredients=extractIngredients(result);

        return ingredients;
    }

    public List<String> extractIngredients(String response) {
        return Arrays.stream(response.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }




}
