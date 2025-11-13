# Netly - Unified Documentation

## 1. Project Overview
Netly is a full-stack application for managing your investment portfolio, supporting asset types like equity, cash, real estate, gold, debt, mutual funds, cryptocurrency, bonds, and more. It features an interactive dashboard, real-time calculations, CRUD operations, filtering, search, and a responsive design. The application is packaged as a single JAR for easy deployment, with both frontend and backend served from the same port.

## 2. Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 17+

> **Note:** Node.js and npm are automatically installed by Maven during the build process.

## 3. Tools Used
- **Spring Boot** (Backend framework)
- **Angular** (Frontend framework)
- **Maven** (Build tool)
- **PostgreSQL** (Database)

## 4. Tech Stack
- **Backend:** Java 17, Spring Boot 3+, PostgreSQL
- **Frontend:** Angular 19, TypeScript
- **Build:** Maven (with frontend-maven-plugin for Angular build)

## 5. Build & Run Steps
### Step 1: Create Database
```bash
psql -U postgres -c "CREATE DATABASE netly_app;"
```

### Step 2: Build & Run
```bash
mvn clean compile install
```

### Step 3: Open Browser
Go to: http://localhost:8080

**Result:** Both frontend and backend are served from port 8080.

#### Run
```bash
java -jar target/netly-app-x.x.x.jar
```

## 6. Configuration
- **Database password:** Edit `netly/src/main/resources/application.properties`:
  ```properties
  spring.datasource.password=YOUR_SECURE_PASSWORD
  ```
- **Change port:**
  ```properties
  server.port=9090
  ```
  Then access at: http://localhost:9090

  ```
- **Run as background service:**
  ```bash
  nohup java -jar netly-app-x.x.x.jar > app.log 2>&1 &
  ```

## 7. Using the Application
- **Dashboard:** View total portfolio value, gain/loss, asset allocation, and breakdown by type.
- **Add Asset:** Use "Add New Asset" to input details for stocks, real estate, gold, etc.
- **Add Liabilities:** Use "Add New Liability" to input details for Home Loans, Car Loans, Personal Loans etc.
- **View/Edit/Delete Assets:** Use "My Assets" to manage your Assets.
- **View/Edit/Delete Liability:** Use "My Liabilities" to manage your Liabilities.
- **Track Performance:** Dashboard auto-calculates gain/loss and allocation.

## 8. Troubleshooting & FAQ
- **Port 8080 already in use:**
  ```bash
  lsof -i :8080
  # or change the port in application.properties
  ```
- **Database connection error:**
  - Ensure PostgreSQL is running and the database exists.
- **Frontend not loading:**
  - Rebuild everything: `mvn clean install && mvn spring-boot:run`
- **Build fails at npm install:**
  - Clean frontend cache: `rm -rf node_modules node package-lock.json` in the frontend directory, then rebuild.
- **Do I need Node.js?** No, Maven handles it.
- **Can I run frontend separately?** Yes: `cd ui && ng serve --port 4200`
- **How do I update the frontend?** Make changes, then run `mvn clean install` in backend.
---

**Happy Investing! 💰📈**

