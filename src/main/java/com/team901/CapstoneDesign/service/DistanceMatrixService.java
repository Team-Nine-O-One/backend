package com.team901.CapstoneDesign.service;

import com.team901.CapstoneDesign.entity.Market;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class DistanceMatrixService {

    private final WebClient webClient;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    public DistanceMatrixService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .build();
    }

    public double getDistance(double startLat, double startLng, double endLat, double endLng) {
        String uri = String.format(
                "/map-direction/v1/driving?start=%f,%f&goal=%f,%f",
                startLng, startLat, endLng, endLat
        );

        try {
            var response = webClient.get()
                    .uri(uri)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> routes = (List<Map<String, Object>>)
                    ((Map<String, Object>) response.get("route")).get("traoptimal");

            return Double.parseDouble(routes.get(0).get("distance").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
    }

    public double[][] buildMatrixFromNaver(List<Market> markets) {
        int n = markets.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            Market m1 = markets.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    Market m2 = markets.get(j);
                    double dist = getDistance(m1.getLatitude(), m1.getLongitude(), m2.getLatitude(), m2.getLongitude());
                    matrix[i][j] = dist;
                }
            }
        }
        return matrix;
    }
    /**
     * @param markets 조합된 마트 목록
     * @return 거리 행렬 (단위: 미터)
     */
    public double[][] buildMatrix(List<Market> markets) {
        int n = markets.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            Market m1 = markets.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    Market m2 = markets.get(j);
                    double dist = haversine(m1.getLatitude(), m1.getLongitude(), m2.getLatitude(), m2.getLongitude());
                    matrix[i][j] = dist;
                }
            }
        }
        return matrix;
    }

    /**
     * Haversine 공식으로 두 지점 간 거리 계산 (단위: 미터)
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // 지구 반지름 (미터)
        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double Δφ = Math.toRadians(lat2 - lat1);
        double Δλ = Math.toRadians(lon2 - lon1);

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2)
                + Math.cos(φ1) * Math.cos(φ2)
                * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
