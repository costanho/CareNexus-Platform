# 🏥 CareNexus Platform

A modular, microservice-ready healthcare platform built with **Spring Boot**, **Docker**, and **MySQL**.  
Designed to manage doctor–patient interactions, appointments, and secure communication using **JWT-based authentication**.

---

## ⚙️ Microservices Overview

| Service | Description | Tech Stack |
|----------|--------------|-------------|
| **Direct Service** | Manages doctors, patients, appointments, and messaging | Spring Boot · JPA · MySQL · Swagger |
| **Auth Service** (coming soon) | Handles user registration, JWT login, and roles | Spring Security · JWT · BCrypt |
| **Notification Service** (planned) | Sends appointment reminders via email/SMS | Spring Mail · Twilio · RabbitMQ |

---

## 🧩 Features Implemented
- RESTful APIs for doctors, patients, appointments, and messages
- Swagger UI documentation (`/swagger-ui.html`)
- MySQL persistence via JPA/Hibernate
- Docker Compose setup for database and service containers
- Modular structure designed for future microservice extraction

---

## 🧠 Learning Goals
This project demonstrates my journey into **full-stack Java development** and **microservice architecture**, focusing on:
- Clean modular design
- Secure authentication
- Continuous integration with GitHub and Docker

---

## 🚀 Quick Start

```bash
# Clone repo
git clone https://github.com/costa-nharingo/CareNexus-Platform.git
cd CareNexus-Platform/carenexus-direct-service

# Run locally
mvn spring-boot:run

# Or use Docker
docker compose up
