package com.example.rag_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // MODELS: Ensure these match what you have installed in Ollama
    private static final String EMBEDDING_MODEL = "nomic-embed-text";
    private static final String CHAT_MODEL = "llama3.2";

    // 1. GENERATE EMBEDDINGS (Local Ollama)
    public float[] getEmbedding(String text) throws Exception {
        URI uri = URI.create("http://localhost:11434/api/embeddings");

        Map<String, Object> body = Map.of(
                "model", EMBEDDING_MODEL,
                "prompt", text
        );

        String req = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(req))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Ollama Embedding Error: " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode embeddingNode = root.get("embedding");

        float[] emb = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            emb[i] = (float) embeddingNode.get(i).asDouble();
        }
        return emb;
    }

    // 2. CHAT COMPLETION (Local Ollama)
    public String chatCompletion(String systemPrompt, String userPrompt) throws Exception {
        URI uri = URI.create("http://localhost:11434/api/chat");

        Map<String, Object> body = Map.of(
                "model", CHAT_MODEL,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
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
            throw new RuntimeException("Ollama Chat Error: " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        return root.at("/message/content").asText();
    }
}