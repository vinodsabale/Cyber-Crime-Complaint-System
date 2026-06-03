# Cyber-Crime Complaint System - Complete Setup & Execution Guide

## ⚡ Project Overview
A comprehensive Spring Boot microservice-based cybercrime complaint system with file upload, criminal search, and officer assignment functionality.

### Tech Stack:
- **Backend**: Spring Boot 3.2.0, Spring Cloud (Eureka, Feign)
- **Database**: MySQL 8.0+
- **Security**: JWT Authentication, Spring Security, BCrypt
- **ORM**: Spring Data JPA, Hibernate
- **File Upload**: MultipartFile with PDF, Image, Document support
- **Frontend**: Thymeleaf, Bootstrap 5
- **API Documentation**: Springdoc OpenAPI/Swagger UI
- **Build**: Maven 3.9+
- **Java Version**: JDK 17+

---

## 🔧 Prerequisites

### System Requirements:
1. **Java 17+** installed (`java -version`)
2. **MySQL 8.0+** server running
3. **Maven 3.9+** (or use included `mvnw` wrapper)
4. **4GB RAM** minimum
5. **Disk Space**: 2GB for Maven dependencies + application

### Install Prerequisites (Ubuntu/Debian):
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk mysql-server -y
```

### Install Prerequisites (Windows):
- Download Java 17 JDK from oracle.com or adoptium.net
- Download MySQL from mysql.com
- Download Maven from maven.apache.org (optional - mvnw included)

---

## 📋 Database Setup

### 1. Create MySQL Database
```bash
mysql -u root -p
```

### 2. Execute SQL Commands:
```sql
CREATE DATABASE IF NOT EXISTS smart_crime_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_crime_db;

-- Tables are automatically created by Hibernate DDL (spring.jpa.hibernate.ddl-auto=update)
```

### 3. MySQL Configuration (application.properties):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_crime_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

---

## 🔐 Security Configuration

### JWT Secret Key (Environment Variable):
```bash
# Linux/Mac
export JWT_SECRET="your-super-secret-key-min-32-chars-for-hs256"

# Windows CMD
set JWT_SECRET=your-super-secret-key-min-32-chars-for-hs256

# Windows PowerShell
$env:JWT_SECRET = "your-super-secret-key-min-32-chars-for-hs256"
```

### Email Configuration (Optional - for notifications):
```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

For Gmail, generate an App Password: https://myaccount.google.com/apppasswords

---

## 🚀 Building the Application

### Option 1: Using Maven Wrapper (Recommended)
```bash
# Navigate to project directory
cd Cyber-Crime-Compalin-System

# Build the application
./mvnw clean install

# Build without running tests (faster)
./mvnw clean install -DskipTests
```

### Option 2: Using System Maven
```bash
mvn clean install -DskipTests
```

### Build Output:
- Success: `target/smart-crime-tracking-1.0.0.jar`
- Size: ~50-80MB (includes all dependencies)

---

## ▶️ Running the Application

### Option 1: Using JAR File
```bash
java -jar target/smart-crime-tracking-1.0.0.jar
```

### Option 2: Using Maven (Development)
```bash
./mvnw spring-boot:run
```

### Option 3: Using IDE
- IntelliJ IDEA / Eclipse: Right-click `CyberCrimeCompalinSystemApplication.java` → Run
- Visual Studio Code: Use Spring Boot Dashboard extension

### Application Starts On:
```
http://localhost:8088
```

---

## 📖 Access Points

| Function | URL | Role | Status |
|----------|-----|------|--------|
| **Home** | http://localhost:8088 | PUBLIC | ✅ |
| **Login** | http://localhost:8088/login | PUBLIC | ✅ |
| **Register** | http://localhost:8088/register | PUBLIC | ✅ |
| **Dashboard** | http://localhost:8088/dashboard | AUTHENTICATED | ✅ |
| **File Complaints** | http://localhost:8088/complaints/file | CITIZEN | ✅ |
| **Track Complaint** | http://localhost:8088/complaints/track | PUBLIC | ✅ |
| **Complaint List** | http://localhost:8088/complaints/list | OFFICER/ADMIN | ✅ |
| **Criminal Search** | http://localhost:8088/suspects/search | OFFICER/ADMIN | ✅ |
| **Swagger API** | http://localhost:8088/swagger-ui.html | PUBLIC | ✅ |
| **API Docs** | http://localhost:8088/v3/api-docs | PUBLIC | ✅ |

---

## 👥 Test User Credentials

### Demo Users (Auto-Created):

#### Citizen User:
- **Email**: citizen@test.com
- **Password**: Test@123
- **Role**: CITIZEN

#### Officer User:
- **Email**: officer@test.com
- **Password**: Test@123
- **Role**: OFFICER

#### Admin User:
- **Email**: admin@test.com
- **Password**: Test@123
- **Role**: ADMIN

#### Analyst User:
- **Email**: analyst@test.com
- **Password**: Test@123
- **Role**: ANALYST

---

## 🔌 API Endpoints (REST)

### Authentication
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login (returns JWT token)
POST   /api/auth/refresh-token     - Refresh JWT token
POST   /api/auth/logout            - Logout
```

### Complaints (REST)
```
POST   /api/complaints             - File new complaint
GET    /api/complaints/{id}        - Get complaint details
PUT    /api/complaints/{id}        - Update complaint
GET    /api/complaints/track/{trackingNumber} - Track complaint
GET    /api/complaints             - List complaints (with pagination)
POST   /api/complaints/{id}/evidence - Upload evidence
```

### Suspects (REST)
```
GET    /api/suspects/search        - Search by phone/email/UPI/account
GET    /api/suspects/{id}          - Get suspect details
POST   /api/suspects               - Create suspect record
PUT    /api/suspects/{id}          - Update suspect
DELETE /api/suspects/{id}          - Delete suspect
```

### Officers (REST)
```
GET    /api/officers               - List officers
POST   /api/officers/{complaintId}/assign - Assign to officer
```

### Documentation
```
GET    /v3/api-docs                - OpenAPI 3.0 JSON spec
GET    /swagger-ui.html            - Swagger UI (interactive)
GET    /actuator/health            - Health check endpoint
```

---

## 📤 File Upload

### Supported File Types:
- **Documents**: pdf, doc, docx
- **Images**: jpg, jpeg, png
- **Other**: txt

### Limits:
- Max file size: 10MB per file
- Max request size: 20MB total
- Upload directory: `./uploads/`

### Upload Endpoints:
```bash
# Using curl
curl -X POST http://localhost:8088/api/complaints/1/evidence \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@complaint.pdf"
```

---

## 🔄 Microservice Configuration

### IMPORTANT - Eureka Service Discovery
**Status**: Disabled for standalone deployment
```properties
eureka.client.enabled=false
```

If you want to enable Eureka (requires Eureka Server on port 8761):
```properties
eureka.client.enabled=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

### Feign Clients (Circuit Breaking):
Feign is configured but not active without service discovery. Enable when using multiple microservices.

---

## 🔍 Database Features

### Auto-Generated Tables:
```sql
- users             (with roles and status)
- complaints        (with tracking number and status)
- evidence          (file storage metadata)
- suspects          (criminal database)
- officers          (police officials)
- notifications     (alerts and updates)
- complaint_status_history (audit trail)
```

### JPA Auditing:
- `createdDate` - Auto-set on creation
- `updatedDate` - Auto-updated on modification
- `createdBy` - Current authenticated user
- `updatedBy` - Last modifier

---

## 🛡️ Security Features

### JWT Token:
- **Expiration**: 24 hours (86400000 ms)
- **Refresh Token**: 7 days (604800000 ms)
- **Algorithm**: HS256

### Password Security:
- **Encryption**: BCrypt with strength 12
- **Minimum**: 8 characters recommended
- **Required**: Uppercase, lowercase, numbers, special chars

### Role-Based Access Control (RBAC):
```
ADMIN   - Full system access, user management
OFFICER - Complaint handling, suspect management
ANALYST - Data analysis, reporting
CITIZEN - File complaints, track status
```

### CORS Configuration:
- Allowed origins: * (configurable)
- Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Credentials: Enabled

---

## 📊 Key Features Verification

### ✅ Complaint Management
- [x] File complaints with personal details
- [x] Multi-file evidence upload (PDF, images, docs)
- [x] Track complaints by tracking number
- [x] Status history and timeline
- [x] Officer assignment and workflow

### ✅ Criminal Search
- [x] Search by phone number
- [x] Search by email address
- [x] Search by UPI ID
- [x] Search by bank account number
- [x] Wanted list view

### ✅ Dashboard Analytics
- [x] Complaint statistics (total, pending, resolved)
- [x] Status distribution
- [x] Recent activities
- [x] Paginated lists with sorting

### ✅ User Management
- [x] Self-registration
- [x] Email verification
- [x] Password reset
- [x] Role-based access

### ✅ API & Integration
- [x] REST API endpoints
- [x] Swagger UI documentation
- [x] Health checks
- [x] Error handling

---

## 🐛 Troubleshooting

### Issue: Cannot connect to database
**Solution**:
```bash
# Check MySQL is running
mysql -u root -p -e "SELECT 1"

# Verify connection string in application.properties
# Default: jdbc:mysql://localhost:3306/smart_crime_db
```

### Issue: Port 8088 already in use
**Solution**:
```bash
# Change in application.properties
server.port=8089

# Or kill the process using port 8088
lsof -ti:8088 | xargs kill -9  # Linux/Mac
netstat -ano | findstr :8088   # Windows
```

### Issue: JWT Token expired
**Solution**: Refresh token using refresh endpoint:
```bash
POST /api/auth/refresh-token
Content-Type: application/json

{"refreshToken": "your_refresh_token_here"}
```

### Issue: Upload folder permission denied
**Solution**:
```bash
mkdir -p ./uploads
chmod 777 ./uploads
```

### Issue: Slow startup
**Reason**: First-time dependency download
**Solution**: Wait 3-5 minutes, subsequent starts are faster

---

## 📝 Important Configuration Changes Made

### ⚠️ Critical Fixes:
1. **DDL Auto**: Changed from `drop` to `update`
   - `drop`: Deletes all tables on startup (DANGEROUS!)
   - `update`: Safely updates schema

2. **Eureka Disabled**: `eureka.client.enabled=false`
   - Removed hard dependency on Eureka Server
   - Application runs standalone

3. **Logging**: Changed from DEBUG to INFO
   - Reduces verbosity and improves startup speed

4. **SQL Logging**: Disabled `show-sql=false`
   - Improves performance, reduces console noise

5. **Email Config**: Moved to environment variables
   - Removes exposed credentials from properties file

6. **Management Endpoints**: Limited exposure
   - Only health and info endpoints public by default

---

## 🚀 Production Deployment

### Pre-Deployment Checklist:
- [ ] Set strong JWT_SECRET (32+ chars)
- [ ] Configure external MySQL database
- [ ] Set environment variables for email
- [ ] Update CORS allowed origins
- [ ] Change default admin password
- [ ] Review logging levels
- [ ] Enable HTTPS
- [ ] Set up backup strategy

### Deployment Example (Linux):
```bash
# Build
./mvnw clean install

# Deploy
java -Dspring.profiles.active=production \
     -DJWT_SECRET="your-secret-key" \
     -jar target/smart-crime-tracking-1.0.0.jar &
```

---

## 📞 Support & Next Steps

### For Issues:
1. Check SETUP_EXECUTION_CHECKLIST.md
2. Review logs: `tail -f nohup.out`
3. Test endpoints: `http://localhost:8088/swagger-ui.html`

### Enhancement Opportunities:
- [ ] Add Redis caching
- [ ] Implement Kafka messaging
- [ ] Enable all microservices with Eureka
- [ ] Add email notifications
- [ ] Implement advanced search
- [ ] Add reporting module

---

## 📄 File Structure
```
Cyber-Crime-Compalin-System/
├── src/main/java/com/
│   ├── config/          (Spring configs, Security, OpenAPI)
│   ├── controller/      (REST & MVC controllers)
│   ├── entity/          (JPA entities)
│   ├── dto/            (Request/Response DTOs)
│   ├── service/        (Business logic)
│   ├── repository/     (Data access)
│   ├── security/       (JWT, authentication)
│   └── exception/      (Custom exceptions)
├── src/main/resources/
│   ├── application.properties  (Main config)
│   └── templates/              (Thymeleaf HTML)
├── pom.xml            (Maven dependencies)
└── mvnw               (Maven wrapper)
```

---

**Last Updated**: June 2, 2026
**Version**: 1.0.0
**Status**: Production Ready ✅
