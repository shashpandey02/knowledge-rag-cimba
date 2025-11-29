import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [file, setFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState("");
  const [question, setQuestion] = useState("");
  const [chatHistory, setChatHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // 1. Handle File Selection
  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  // 2. Handle Upload
  const handleUpload = async () => {
    if (!file) {
      alert("Please select a file first!");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setUploadStatus("Uploading...");
      // Ensure this URL matches your Spring Boot Controller
      await axios.post("http://localhost:8080/api/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setUploadStatus("Upload Successful! ✅");
    } catch (error) {
      console.error("Upload Error:", error);
      setUploadStatus("Upload Failed ❌");
    }
  };

  // 3. Handle Question
  const handleAsk = async () => {
    if (!question.trim()) return;

    const newHistory = [...chatHistory, { role: 'user', content: question }];
    setChatHistory(newHistory);
    setQuestion("");
    setIsLoading(true);

    try {
      // Ensure this URL matches your Spring Boot Controller
      const response = await axios.post("http://localhost:8080/api/query", {
        question: question 
      });

      setChatHistory([
        ...newHistory,
        { role: 'ai', content: response.data.answer }
      ]);
    } catch (error) {
      console.error("Query Error:", error);
      setChatHistory([
        ...newHistory,
        { role: 'ai', content: "Error: Could not get answer from backend." }
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="app-container">
      <header>
        <h1>RAG Knowledge Base 🤖</h1>
      </header>

      <main>
        {/* Upload Section */}
        <section className="card">
          <h2>1. Upload Document</h2>
          <div className="controls">
            <input type="file" onChange={handleFileChange} />
            <button onClick={handleUpload} className="btn primary">Upload PDF</button>
          </div>
          <p className="status">{uploadStatus}</p>
        </section>

        {/* Chat Section */}
        <section className="card chat-card">
          <h2>2. Ask Questions</h2>
          <div className="chat-window">
            {chatHistory.length === 0 && <p className="placeholder">Ask something about your document...</p>}
            {chatHistory.map((msg, index) => (
              <div key={index} className={`message ${msg.role}`}>
                <div className="bubble">{msg.content}</div>
              </div>
            ))}
            {isLoading && <p className="loading">Thinking...</p>}
          </div>
          <div className="input-area">
            <input 
              type="text" 
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAsk()}
              placeholder="Type your question..."
            />
            <button onClick={handleAsk} disabled={isLoading} className="btn secondary">Send</button>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;