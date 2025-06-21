package com.team901.CapstoneDesign.GPT.service;


import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Value
public class YouTubeSTTProcessor {

    private final IngredientExtractionService ingredientExtractionService;

    /**
     * YouTube Shorts 영상을 다운로드하고 Whisper API로 STT 처리한 뒤 결과를 IngredientExtractionService로 전달
     */
    public List<String> processShorts(String youtubeUrl) {
        try {
            // 1. 유튜브 영상 다운로드 (ffmpeg 또는 youtube-dl을 사용한 파일 저장. 예시로 로컬 파일 경로 사용)
            File audioFile = downloadYouTubeAudio(youtubeUrl);

            // 2. Whisper API로 STT 요청
            String transcription = callWhisperAPI(audioFile);

            // 3. 결과를 IngredientExtractionService로 전달
            return ingredientExtractionService.extractIngredients(transcription);

        } catch (Exception e) {
            log.error("YouTube STT 처리 중 오류 발생: {}", e.getMessage());
        }
        return ingredientExtractionService.extractIngredients(youtubeUrl);
    }


    private String callWhisperAPI(File file) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("model", "whisper-1");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth("sk-xxxxxxxxxxxx"); // API 키 또는 @Value("${openai.api.key}")

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/audio/transcriptions",
                requestEntity,
                String.class
        );

        return response.getBody(); // JSON 형태면 Jackson으로 파싱도 가능
    }

    private File downloadYouTubeAudio(String url) throws Exception {
        // 저장 폴더 및 파일 이름 설정
        File downloadDir = new File("downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs(); // 디렉토리 없으면 생성
        }

        String outputTemplate = "downloads/audio.%(ext)s"; // yt-dlp가 .mp3로 저장할 것임

        // yt-dlp 명령어 구성
        ProcessBuilder builder = new ProcessBuilder(
                "yt-dlp",
                "-x", // extract audio
                "--audio-format", "mp3", // mp3로 변환
                "-o", outputTemplate, // 출력 파일명 형식
                url
        );

        // 오류 출력도 표준 출력으로 합치기
        builder.redirectErrorStream(true);
        builder.directory(new File(".")); // 현재 프로젝트 폴더

        // 프로세스 실행
        Process process = builder.start();

        // 출력 스트림을 보기 원하면 다음과 같이 처리 (선택)
        new Thread(() -> {
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[yt-dlp] " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp 실행 실패: 종료 코드 " + exitCode);
        }

        // yt-dlp는 기본적으로 audio.mp3로 저장하므로 해당 파일 반환
        File mp3File = new File("downloads/audio.mp3");
        if (!mp3File.exists()) {
            throw new RuntimeException("오디오 파일 생성 실패: downloads/audio.mp3 없음");
        }

        return mp3File;
    }

}
