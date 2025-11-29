
# 🧠 Knowledge Base RAG System


A full-stack Retrieval-Augmented Generation (RAG) application that allows users to upload PDF documents and ask natural language questions about their content. The system uses Google Gemini for embeddings and generation, and PostgreSQL (pgvector) for efficient vector retrieval.

🚀 Features
📄 Document Ingestion: Upload PDF files which are automatically parsed, chunked, and stored.

🔍 Semantic Search: Uses vector embeddings to find the most relevant document sections for any query.

🤖 AI-Powered Answers: Generates accurate, context-aware answers using Google's Gemini 2.0 Flash model.

🖥️ Interactive UI: Clean React-based interface for uploading files and chatting with your data.

## Tech Stack

**Frontend:** React, Axios, CSS3

**Backend:** SpringBoot, Java JDBC, Maven

**Others:** PostgreSQL(with pg vector), Google Gemini API


## ⚡ Installation & Setup

1. Database Setup

Make sure PostgreSQL is running and enable the vector extension.

```bash
    CREATE DATABASE rag_db;
    \c rag_db;
    CREATE EXTENSION vector;

    CREATE TABLE doc_chunks (
        id BIGSERIAL PRIMARY KEY,
        document_id BIGINT,
        chunk_index INT,
        chunk_text TEXT,
        embedding vector(768) -- Matches text-embedding-004 dimension
    );
```
2. Backend Setup (Spring Boot)

Navigate to the backend directory.

Open src/main/resources/application.properties and configure your credentials:

```bash
    spring.datasource.url=jdbc:postgresql://localhost:5432/rag_db
    spring.datasource.username=your_postgres_user
    spring.datasource.password=your_postgres_password
    gemini.api.key=YOUR_GEMINI_API_KEY
```
3. Run the application:

```bash
    ./mvnw spring-boot:run
```
Server will start on http://localhost:8080

4. Frontend Setup (React)

Navigate to the frontend (or rag-frontend) directory.

Install dependencies:

```bash
npm install
```
Start the development server:

```bash
npm start
```

App will open at http://localhost:3000

<img width="1708" height="462" alt="architectural design" src="https://github.com/user-attachments/assets/3ba74bfd-5195-4fe5-a7be-f64fe28ba899" />





    
