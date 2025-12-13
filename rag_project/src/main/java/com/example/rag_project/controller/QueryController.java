package com.example.rag_project.controller;

import com.example.rag_project.service.OllamaService;
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

    private final OllamaService aiService; // <--- CHANGED FIELD
    private final VectorStoreService vectorStore;

    // Inject OllamaService instead of GeminiService
    public QueryController(OllamaService aiService, VectorStoreService vectorStore) {
        this.aiService = aiService;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody Map<String, String> payload) {
        try {
            String question = payload.get("question");
            if (question == null || question.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "question required"));

            // 1. Get Embedding (Using Ollama)
            float[] emb = aiService.getEmbedding(question);

            // 2. Search Database (Logic remains exactly the same)
            List<Map<String,Object>> nearest = vectorStore.findNearest(emb, 5);

            // 3. Build Context from found chunks
            String context = nearest.stream()
                    .map(r -> "Chunk: " + r.get("chunk_text"))
                    .collect(Collectors.joining("\n\n"));

            String systemPrompt = "You are a helpful assistant. Use the context below to answer. If unsure, say 'I don't know'.";
            String userPrompt = "Context:\n" + context + "\n\nQuestion: " + question;

            // 4. Generate Answer (Using Ollama Llama 3.2)
            String answer = aiService.chatCompletion(systemPrompt, userPrompt);

            return ResponseEntity.ok(Map.of(
                    "answer", answer,
                    "sources", nearest
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}