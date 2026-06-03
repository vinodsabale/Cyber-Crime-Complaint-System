# Cyber-Crime-Complaint-System
Cyber-Crime-Complaint-System
# 🛡️ Cyber Crime Complaint System

A full-stack web application for managing cyber crime complaints, built with **Spring Boot**, **Thymeleaf**, **MySQL**, and **Spring Security**.

---

## 🚀 Features

- **Citizen Portal** — Register, login, file complaints, track status
- **Officer Dashboard** — Manage assigned cases, update status
- **Admin Panel** — Manage users, officers, suspects, wanted persons
- **JWT Authentication** — Secure REST API with JWT tokens
- **Role-Based Access** — CITIZEN, OFFICER, ANALYST, ADMIN roles
- **Evidence Upload** — Attach files/images to complaints
- **Suspect Management** — Track suspects linked to complaints
- **Wanted List** — Maintain wanted persons linked to complaints
- **Notifications** — In-app notification system
- **Cache Support** — Redis/Simple cache for performance
- **Swagger UI** — API documentation

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Frontend | Thymeleaf, Bootstrap 5, Bootstrap Icons |
| Database | MySQL 8 |
| Auth | JWT + Spring Session |
| Cache | Spring Cache (Redis ready) |
| Build | Maven |
| Java | Java 21 |

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 21+
- MySQL 8+
- Maven 3.8+

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/Cyber-Crime-Complaint-System.git
cd Cyber-Crime-Complaint-System
```

### 2. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/practice
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Access the app
| URL | Description |
|-----|-------------|
| http://localhost:8088 | Home Page |
| http://localhost:8088/login | Login |
| http://localhost:8088/register | Register |
| http://localhost:8088/dashboard | Dashboard |
| http://localhost:8088/swagger-ui.html | API Docs |

---

## 🔐 Default Admin Credentials

| Field | Value |
|-------|-------|
| Email | `admin@crime.gov.in` |
| Password | `Admin@1234` |

> ⚠️ Change the password after first login!

---

## 📁 Project Structure

```
src/main/java/com/
├── config/          # Security, Redis, Audit config
├── controller/
│   ├── mvc/         # Thymeleaf controllers
│   └── rest/        # REST API controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA Entities
├── exception/       # Custom exceptions
├── repository/      # Spring Data repositories
├── security/        # JWT filter, UserDetailsService
├── service/         # Business logic
└── util/            # Utility classes
```

---

## 📌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/register` | Register |
| GET | `/api/complaints` | Get all complaints |
| POST | `/api/complaints` | File new complaint |
| GET | `/api/suspects` | Get all suspects |
| GET | `/admin/wanted` | Wanted persons list |

Full API docs available at `/swagger-ui.html`

---

## 🙋 Author

Made with ❤️ for academic/demo purposes.
