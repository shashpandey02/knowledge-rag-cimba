package com.example.rag_project.controller;

import com.example.rag_project.entity.Document;
import com.example.rag_project.repository.DocumentRepository;
import com.example.rag_project.service.GeminiService;
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
    private final GeminiService geminiService;
    private final VectorStoreService vectorStore;
    private final DocumentRepository documentRepo;

    public UploadController(TextExtractionService textService,
                            GeminiService geminiService,
                            VectorStoreService vectorStore,
                            DocumentRepository documentRepo) {
        this.textService = textService;
        this.geminiService = geminiService;
        this.vectorStore = vectorStore;
        this.documentRepo = documentRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc = documentRepo.save(doc);

        String text = textService.extractTextFromPdf(file);
        List<String> chunks = textService.chunkText(text, 800, 150);

        for (int i = 0; i < chunks.size(); i++) {
            float[] emb = geminiService.getEmbedding(chunks.get(i));
            vectorStore.insertChunkEmbedding(doc.getId(), i, chunks.get(i), emb);
        }

        return ResponseEntity.ok(Map.of(
                "documentId", doc.getId(),
                "chunks", chunks.size()
        ));
    }
}
