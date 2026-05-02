
# DBSight Backend 🚀

Backend service for DBSight — an AI-powered database query and insights platform.  
Built using Spring Boot, PostgreSQL, Redis, and Docker.

---

# Demo




https://github.com/user-attachments/assets/4db59b86-7f67-43ef-b397-91f35628f9b4



[screen-capture (1).webm](https://github.com/user-attachments/assets/ff78da88-b28d-4477-94a4-c97282eec494)

---

## 🧠 What This Does

DBSight Backend allows users to:
- Convert natural language → SQL queries using AI
- Execute queries safely on connected databases
- Apply rate limiting for AI usage
- Cache results for performance
- Store query history

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot
- Spring Security (JWT Auth)
- PostgreSQL (Primary DB)
- Redis (Caching + Rate Limiting)
- Docker & Docker Compose
- pgvector (for embeddings / AI context)
- REST APIs

---

## ⚙️ Features

### 🔹 AI Query Engine
- Converts user prompts → SQL queries
- Handles structured query generation
- Error handling for malformed prompts

### 🔹 Rate Limiting
- Daily AI request cap per user
- Prevents abuse and controls cost

### 🔹 Caching (Redis)
- Frequently used query results cached
- Reduces DB load + improves speed

### 🔹 Authentication

- JWT-based user authentication
- Secure API endpoints

### 🔹 Query History
- Stores past queries per user
- Useful for reuse & insights


## 🧱 Architecture
Client → Controller → Service → Component -> featrue layes → DB
                                                  ↓
                                                 Redis(to cache user details)
                                                

