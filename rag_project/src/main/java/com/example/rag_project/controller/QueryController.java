package com.example.rag_project.controller;

import com.example.rag_project.service.GeminiService;
import com.example.rag_project.service.VectorStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class QueryController {

    private final GeminiService geminiService;
    private final VectorStoreService vectorStore;

    public QueryController(GeminiService geminiService, VectorStoreService vectorStore) {
        this.geminiService = geminiService;
        this.vectorStore = vectorStore;
    }

    // NEW DEBUG ENDPOINT
    @GetMapping("/models")
    public ResponseEntity<String> listModels() {
        return ResponseEntity.ok(geminiService.listAvailableModels());
    }

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody Map<String, String> payload) {
        try {
            String question = payload.get("question");
            if (question == null || question.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "question required"));

            // 1. Get Embedding
            float[] emb = geminiService.getEmbedding(question);

            // 2. Search Database
            List<Map<String,Object>> nearest = vectorStore.findNearest(emb, 5);

            // 3. Build Context
            String context = nearest.stream()
                    .map(r -> "Chunk: " + r.get("chunk_text"))
                    .collect(Collectors.joining("\n\n"));

            String systemPrompt = "You are a helpful assistant. Use the context below to answer.";
            String userPrompt = "Context:\n" + context + "\n\nQuestion: " + question;

            // 4. Generate Answer
            String answer = geminiService.chatCompletion(systemPrompt, userPrompt);

            return ResponseEntity.ok(Map.of("answer", answer, "sources", nearest));

        } catch (Exception e) {
            e.printStackTrace(); // Print full error to console
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}