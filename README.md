# 🥗 NutriGen Backend Service

Spring Boot 3.5 REST API for AI-powered food image analysis, nutrition tracking, and diet recommendations.

---

## 🚀 Render Deployment (Web Service)

This repository is pre-configured for automated deployment on **Render** as a **Web Service** using Docker.

### Option 1: Blueprint Deployment (Automatic)
1. In the [Render Dashboard](https://dashboard.render.com/), click **New +** > **Blueprint**.
2. Connect this repository. Render will automatically read `render.yaml`.
3. Provide the required database and secret environment variables when prompted.

### Option 2: Manual Web Service
1. In the [Render Dashboard](https://dashboard.render.com/), click **New +** > **Web Service**.
2. Connect this repository.
3. Configure the service settings:
   - **Environment / Runtime**: `Docker`
   - **Region**: Oregon (or your preferred region)
   - **Health Check Path**: `/api/health`
4. Under **Environment Variables**, add the variables listed below.

---

## 🔑 Required Render Environment Variables

| Variable Name | Description | Example / Note |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | Set to `prod` |
| `PORT` | Web port assigned by Render | Automatically supplied by Render (or default `10000`) |
| `DB_URL` | MySQL JDBC Connection URL | `jdbc:mysql://<host>:<port>/<database>?useSSL=true&requireSSL=false&serverTimezone=UTC` |
| `DB_USER` | MySQL Database Username | Database user |
| `DB_PASS` | MySQL Database Password | Database password |
| `FRONTEND_URL` | Render Frontend Static Site URL | `https://nutrigen-frontend.onrender.com` (for CORS) |
| `JWT_SECRET` | Secret key for tokens/signing | Any secure 32+ character random string |
| `GEMINI_API_KEY` | *(Optional)* Google Gemini Vision API Key | Free API key from Google AI Studio (fallback heuristics used if omitted) |

---

## 💻 Local Development

### Prerequisites
* Java 17+ or Java 21+
* MySQL 8 running on port 3306

### Run with Maven / JAR
```bash
java -jar target/nutrigen-backend-1.0.0.jar
```
API runs locally on `http://localhost:8080`.
Health check: `http://localhost:8080/api/health`.
