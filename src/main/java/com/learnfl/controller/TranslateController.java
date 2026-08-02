package com.learnfl.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfl.common.Result;
import com.learnfl.dto.manage.TranslateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 翻译代理：调用 MyMemory 免费翻译 API（无密钥、免注册）。
 * 前端直接调第三方翻译 API 会有 CORS 限制，走后端代理统一处理。
 */
@Slf4j
@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslateController {

    private static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @PostMapping
    public Result<String> translate(@Valid @RequestBody TranslateRequest req) {
        try {
            // langpair 参数格式为 ja|en，| 在 URL 中需编码为 %7C
            String url = MYMEMORY_URL
                    + "?q=" + URLEncoder.encode(req.getText(), StandardCharsets.UTF_8)
                    + "&langpair=" + req.getFrom() + "%7C" + req.getTo();

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(Duration.of(8, ChronoUnit.SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("MyMemory 返回异常状态码: {}", response.statusCode());
                return Result.error(502, "翻译服务暂时不可用");
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (root.path("responseStatus").asInt() != 200) {
                return Result.error(502, "翻译失败: " + root.path("responseDetails").asText());
            }

            String result = root.path("responseData").path("translatedText").asText().trim();
            if (result.isEmpty() || "QUERY LENGTH LIMIT EXCEEDED".equals(result)) {
                return Result.error(502, "翻译结果为空");
            }
            return Result.ok(result);
        } catch (Exception e) {
            log.warn("翻译接口异常", e);
            return Result.error(502, "翻译服务暂时不可用");
        }
    }
}
