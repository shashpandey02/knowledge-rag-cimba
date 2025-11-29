package com.example.rag_project.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextExtractionService {

    public String extractTextFromPdf(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             PDDocument pdf = PDDocument.load(is)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < words.length; i++) {
            sb.append(words[i]).append(" ");
            count++;

            if (count >= chunkSize) {
                chunks.add(sb.toString().trim());

                // prepare next chunk with overlap
                sb.setLength(0);
                int start = Math.max(0, i - overlap);
                for (int j = start; j <= i; j++) {
                    sb.append(words[j]).append(" ");
                }

                count = overlap; // we already placed overlap words
            }
        }

        if (!sb.isEmpty()) {
            chunks.add(sb.toString().trim());
        }

        return chunks;
    }


}
