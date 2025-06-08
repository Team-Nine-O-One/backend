package com.team901.CapstoneDesign.youtube;

import com.google.api.services.youtube.model.Video;
import com.team901.CapstoneDesign.GPT.dto.YoutubeContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class YouTubeController {

    private final YouTubeDataService youtubeDataService;

//    @GetMapping("/youtube/info")
//    public ResponseEntity<?> getVideoInfo(@RequestParam String videoId) throws IOException {
//        Video video = youtubeDataService.getVideoInfo(videoId);
//        if (video == null) return ResponseEntity.notFound().build();
//
//        String title = video.getSnippet().getTitle();
//        String description = video.getSnippet().getDescription();
//
//        return ResponseEntity.ok(Map.of(
//                "title", title,
//                "description", description
//        ));
//    }





    @GetMapping("/crawler/youtube")
    @Operation(
            summary = "유튜브 쇼츠에서 재료 추출",
            description = "유튜브 video id 를 입력하면 영상 설명을 분석하여 요리 재료 목록을 추출하고 영상 제목과 함께 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 재료를 추출하였습니다."),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
                    @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
            }
    )
    public YoutubeContent youtube(@Parameter(description = "유튜브 쇼츠 영상 url", example = "https://www.youtube.com/shorts/5CuOskioLNE")
                                  @RequestParam String url){
        try {
            return youtubeDataService.getYoutubeVideoInfo(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
