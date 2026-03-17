# Spring Boot AWS Docker — Job Portal REST API

A Spring Boot REST API containerized with Docker and PostgreSQL, pushed to AWS ECR, and deployed as a multi-container service on AWS ECS Fargate.

## 🚀 Live Demo
Deployed on AWS ECS Fargate  
**Base URL:** `http://<your-ecs-public-ip>:8080`

---

## 📌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/load` | Load initial sample job data into DB |
| GET | `/jobPosts` | Get all job posts |
| GET | `/jobPost/{postId}` | Get a specific job post by ID |
| POST | `/jobPost` | Add a new job post |
| PUT | `/jobPost` | Update an existing job post |
| DELETE | `/jobPost/{postId}` | Delete a job post by ID |
| GET | `/jobPosts/keyword/{keyword}` | Search jobs by keyword |

### Example Response - `GET /jobPosts`
```json
[
  {
    "postId": 1,
    "postProfile": "Java Developer",
    "postDesc": "Must have good experience in core Java and advanced Java",
    "reqExperience": 2,
    "postTechStack": ["Core Java", "J2EE", "Spring Boot", "Hibernate"]
  },
  {
    "postId": 2,
    "postProfile": "Frontend Developer",
    "postDesc": "Experience in building responsive web applications using React",
    "reqExperience": 3,
    "postTechStack": ["HTML", "CSS", "JavaScript", "React"]
  }
]
```

---

## 🛠️ Tech Stack

- **Java 21** - Programming language
- **Spring Boot 4.x** - Web framework
- **Spring Data JPA** - Database ORM
- **Hibernate** - JPA implementation
- **Lombok** - Reduces boilerplate code
- **PostgreSQL 15** - Relational database
- **Maven** - Build tool
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **AWS ECR** - Private Docker image registry
- **AWS ECS Fargate** - Serverless container deployment

---

## 📁 Project Structure

```
spring-aws-docker/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/chetanjogi/springawsdocker/
│       │       ├── SpringAwsDockerApplication.java
│       │       ├── controller/
│       │       │   └── JobController.java        ← REST endpoints
│       │       ├── model/
│       │       │   └── JobPost.java              ← Entity/DB table
│       │       ├── repo/
│       │       │   └── JobRepo.java              ← JPA Repository
│       │       └── service/
│       │           └── JobService.java           ← Business logic
│       └── resources/
│           └── application.properties            ← App config
├── Dockerfile                                    ← Container build instructions
├── docker-compose.yml                            ← Multi-container setup
├── .env.example                                  ← Environment variable template
├── .env                                          ← Real secrets (NOT in GitHub)
├── .gitignore
└── pom.xml
```

---

## ☁️ Architecture

```
[Browser / Postman]
        ↓
[AWS ECS Fargate Task]
   ┌────────────────────────────────┐
   │  Container 1: spring-app       │  ← Spring Boot (port 8080)
   │  Container 2: postgres-db      │  ← PostgreSQL  (port 5432)
   └────────────────────────────────┘
        ↑
[AWS ECR] stores the Docker image
```

Both containers run inside the same ECS Task and communicate via `localhost` since they share the same network namespace in Fargate.

---

## ⚙️ How to Run Locally

### Prerequisites
- Java 21+
- Maven 3.x
- Docker Desktop (running)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/chetanjogi/spring-aws-docker.git
cd spring-aws-docker

# 2. Create your .env file from the template
cp .env.example .env
# Edit .env and fill in your own password

# 3. Build the JAR
mvn clean package -DskipTests

# 4. Start both containers
docker-compose up --build

# 5. Load initial data (run this first!)
# GET http://localhost:8080/load

# 6. Test endpoints
# GET http://localhost:8080/jobPosts
```

### Stop containers
```bash
docker-compose down

# Stop and wipe database
docker-compose down -v
```

---

## 🔐 Environment Variables

This project uses a `.env` file for secrets. **Never commit your real `.env` to GitHub.**

Copy `.env.example` and fill in your values:

```bash
cp .env.example .env
```

`.env.example` template:
```env
POSTGRES_DB=Spring_Aws
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password_here
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/Spring_Aws
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password_here
```

---

## 🐳 Docker Setup

### Dockerfile
```dockerfile
FROM amazoncorretto:21
WORKDIR /app
COPY target/springboot-aws-docker.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
The compose file spins up two services:
- `db` — PostgreSQL 15 with persistent volume storage
- `app` — Spring Boot app that connects to `db` by service name

Both services are connected via a custom Docker bridge network (`spring-network`) so they can communicate with each other by service name.

---

## ☁️ AWS Deployment Steps

### Step 1 — Build JAR
```bash
mvn clean package -DskipTests
# Output: target/springboot-aws-docker.jar
```

### Step 2 — Create ECR Repository
1. AWS Console → ECR → Create repository
2. Name: `springboot-aws-docker`
3. Visibility: Private
4. Click **Create**

### Step 3 — Push Image to ECR
Click **"View push commands"** in ECR and run the 4 commands provided:
```bash
# 1. Authenticate Docker to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# 2. Build image
docker build -t springboot-aws-docker .

# 3. Tag image
docker tag springboot-aws-docker:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/springboot-aws-docker:latest

# 4. Push to ECR
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/springboot-aws-docker:latest
```

### Step 4 — Create ECS Cluster
1. AWS Console → ECS → Create Cluster
2. Name: `springboot-docker-cluster`
3. Infrastructure: **Fargate (Serverless)**
4. Click **Create**

### Step 5 — Create Task Definition
1. ECS → Task Definitions → Create new
2. Family name: `springboot-docker-task`
3. Launch type: **Fargate**
4. CPU: `0.5 vCPU`, Memory: `1 GB`

**Container 1 — Spring Boot App:**
| Field | Value |
|-------|-------|
| Name | `spring-app` |
| Image URI | Your ECR image URI |
| Port | `8080` |
| SPRING_DATASOURCE_URL | `jdbc:postgresql://localhost:5432/Spring_Aws` |
| SPRING_DATASOURCE_USERNAME | `postgres` |
| SPRING_DATASOURCE_PASSWORD | `your_password` |

**Container 2 — PostgreSQL:**
| Field | Value |
|-------|-------|
| Name | `postgres-db` |
| Image URI | `postgres:15` |
| Port | `5432` |
| POSTGRES_DB | `Spring_Aws` |
| POSTGRES_USER | `postgres` |
| POSTGRES_PASSWORD | `your_password` |

> ⚠️ Use `localhost` (not `db`) for the datasource URL in ECS — both containers share the same network namespace inside a Fargate task

### Step 6 — Create ECS Service
1. Go to cluster → Services → Create
2. Launch type: **Fargate**
3. Task definition: `springboot-docker-task`
4. Service name: `springboot-service`
5. Desired tasks: `1`
6. Enable **Public IP** ✅
7. Click **Create**

### Step 7 — Open Port 8080
1. EC2 → Security Groups
2. Find the security group used by your ECS task
3. Edit inbound rules → Add rule:
   - Type: Custom TCP
   - Port: `8080`
   - Source: `0.0.0.0/0`

### Step 8 — Test Live Endpoints
```
GET http://<ecs-public-ip>:8080/load
GET http://<ecs-public-ip>:8080/jobPosts
```

---

## 🧪 Testing with Postman

### Load Data First
```
GET /load
```

### Add a New Job
```
POST /jobPost
Content-Type: application/json

{
  "postId": 6,
  "postProfile": "DevOps Engineer",
  "postDesc": "Experience with CI/CD pipelines and cloud infrastructure",
  "reqExperience": 4,
  "postTechStack": ["Docker", "Kubernetes", "Jenkins", "AWS"]
}
```

### Search Jobs
```
GET /jobPosts/keyword/Java
```

### Delete a Job
```
DELETE /jobPost/1
```

---

## 💰 AWS Cost Warning

| Service | Cost |
|---------|------|
| ECR | 500MB free storage |
| ECS Fargate | ⚠️ Charges per vCPU/memory per second — NOT free tier |
| Data transfer | Small cost |

> ⚠️ **Always delete your ECS service and cluster when not in use to avoid charges!**

### Cleanup Commands (AWS Console)
1. ECS → Services → `springboot-service` → **Delete service**
2. ECS → Clusters → `springboot-docker-cluster` → **Delete cluster**
3. ECR → `springboot-aws-docker` → **Delete repository** (optional)

---

## 🔧 .gitignore highlights

```
target/       ← compiled output, not pushed
*.jar         ← built JAR files, not pushed
.env          ← secrets, NEVER pushed to GitHub
.idea/        ← IDE files, not pushed
```

---

## 👤 Author
**Chetanjogi**
- GitHub: [@chetanjogi](https://github.com/chetanjogi)

---

## 📚 What I Learned
- Containerizing a Spring Boot application with Docker
- Running multi-container apps with Docker Compose
- Docker networking — containers communicating by service name
- Pushing Docker images to AWS ECR (Elastic Container Registry)
- Creating ECS clusters, task definitions, and services
- Deploying multi-container apps on AWS ECS Fargate (serverless)
- Managing secrets with `.env` files and keeping them out of GitHub
- Configuring AWS security groups to open ports for public access
- Testing live REST APIs deployed on cloud infrastructure with Postman
