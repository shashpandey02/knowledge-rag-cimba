package com.example.rag_project.controller;

import com.example.rag_project.entity.Document;
import com.example.rag_project.repository.DocumentRepository;
import com.example.rag_project.service.OllamaService;
import com.example.rag_project.service.TextExtractionService;
import com.example.rag_project.service.VectorStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UploadController {

    private final TextExtractionService textService;
    private final OllamaService aiService; // <--- CHANGED FIELD
    private final VectorStoreService vectorStore;
    private final DocumentRepository documentRepo;

    // Inject OllamaService instead of GeminiService
    public UploadController(TextExtractionService textService,
                            OllamaService aiService,
                            VectorStoreService vectorStore,
                            DocumentRepository documentRepo) {
        this.textService = textService;
        this.aiService = aiService;
        this.vectorStore = vectorStore;
        this.documentRepo = documentRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {

        // 1. Save Document Metadata
        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc = documentRepo.save(doc);

        // 2. Extract Text
        String text = textService.extractTextFromPdf(file);

        // 3. Chunk Text
        List<String> chunks = textService.chunkText(text, 800, 150);

        // 4. Generate Embeddings & Store (Using Ollama)
        for (int i = 0; i < chunks.size(); i++) {
            // Using Ollama to get the embedding vector
            float[] emb = aiService.getEmbedding(chunks.get(i));

            // Storing in Postgres
            vectorStore.insertChunkEmbedding(doc.getId(), i, chunks.get(i), emb);
        }

        return ResponseEntity.ok(Map.of(
                "documentId", doc.getId(),
                "chunks", chunks.size(),
                "message", "File uploaded and processed successfully using Ollama"
        ));
    }
}