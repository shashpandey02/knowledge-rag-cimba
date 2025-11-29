package com.example.rag_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 1. EMBEDDING METHOD (This was working fine)
    public float[] getEmbedding(String text) throws Exception {
        URI uri = URI.create(
                "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=" + apiKey
        );

        Map<String, Object> body = Map.of(
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        String req = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(req))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Embedding error: " + resp.body());
        }

        JsonNode values = mapper.readTree(resp.body()).at("/embedding/values");
        float[] emb = new float[values.size()];
        for (int i = 0; i < values.size(); i++)
            emb[i] = (float) values.get(i).asDouble();

        return emb;
    }

    // 2. CHAT METHOD (Updated model name)
     public String chatCompletion(String systemPrompt, String userPrompt) throws Exception {
            // CHANGED: Using "gemini-2.0-flash" because it is in your allowed list
            String modelName = "gemini-2.0-flash";

            URI uri = URI.create(
                    "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey
            );

            String finalPrompt = systemPrompt + "\n\n" + userPrompt;

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", finalPrompt)))
                    )
            );

            String req = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(req))
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                System.err.println("Gemini Error: " + resp.body());
                throw new RuntimeException("Chat error (" + resp.statusCode() + "): " + resp.body());
            }

            JsonNode root = mapper.readTree(resp.body());
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

            if (textNode.isMissingNode()) {
                return "No response text found.";
            }

            return textNode.asText();
        }


    // 3. DEBUG METHOD: List available models
    public String listAvailableModels() {
        try {
            URI uri = URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (Exception e) {
            return "Failed to list models: " + e.getMessage();
        }
    }
}