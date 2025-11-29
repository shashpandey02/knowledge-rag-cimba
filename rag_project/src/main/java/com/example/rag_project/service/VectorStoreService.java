package com.example.rag_project.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class VectorStoreService {

    private final JdbcTemplate jdbc;

    public VectorStoreService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // NEW HELPER: Convert float[] to String "[0.1,0.2,...]"
    private String vectorToString(float[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public void insertChunkEmbedding(Long docId, int index, String text, float[] embedding) {
        String embeddingStr = vectorToString(embedding); // Convert to String

        jdbc.update("""
                INSERT INTO doc_chunks (document_id, chunk_index, chunk_text, embedding)
                VALUES (?, ?, ?, ?::vector)
                """,
                docId, index, text, embeddingStr // Pass String
        );
    }

    public List<Map<String,Object>> findNearest(float[] queryEmb, int limit) {
        String embeddingStr = vectorToString(queryEmb); // Convert to String

        // Note: We use ?::vector to tell Postgres this string is a vector
        return jdbc.queryForList("""
                SELECT document_id, chunk_index, chunk_text,
                       embedding <-> ?::vector AS distance
                FROM doc_chunks
                ORDER BY embedding <-> ?::vector
                LIMIT ?
                """,
                embeddingStr, embeddingStr, limit
        );
    }
}