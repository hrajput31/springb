package com.example.webhook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Map;

@Component
public class AppRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;

    public AppRunner(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public void run(String... args) {
        String initUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> requestBody = Map.of(
            "name", "John Doe",
            "regNo", "REG12347",
            "email", "john@example.com"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(initUrl, requestBody, Map.class);

        String accessToken = (String) response.getBody().get("accessToken");
        String webhookUrl = (String) response.getBody().get("webhook");

        String finalQuery = """
            SELECT 
                P.AMOUNT AS SALARY,
                CONCAT(E.FIRST_NAME, ' ', E.LAST_NAME) AS NAME,
                TIMESTAMPDIFF(YEAR, E.DOB, CURDATE()) AS AGE,
                D.DEPARTMENT_NAME
            FROM PAYMENTS P
            JOIN EMPLOYEE E ON P.EMP_ID = E.EMP_ID
            JOIN DEPARTMENT D ON E.DEPARTMENT = D.DEPARTMENT_ID
            WHERE DAY(P.PAYMENT_TIME) != 1
            ORDER BY P.AMOUNT DESC
            LIMIT 1
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, String> finalBody = Map.of("finalQuery", finalQuery);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(finalBody, headers);

        restTemplate.postForEntity(webhookUrl, entity, String.class);
    }
}
