# Netly - Unified Documentation

## 1. Project Overview
Netly is a full-stack application for managing your investment portfolio, supporting asset types like equity, cash, real estate, gold, debt, mutual funds, cryptocurrency, bonds, and more. It features an interactive dashboard, real-time calculations, CRUD operations, filtering, search, and a responsive design. The application is packaged as a single JAR for easy deployment, with both frontend and backend served from the same port.

## 2. Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

> **Note:** Node.js and npm are automatically installed by Maven during the build process.

## 3. Tools Used
- **Spring Boot** (Backend framework)
- **Angular** (Frontend framework)
- **Maven** (Build tool)
- **PostgreSQL** (Database)
- **Chart.js + ng2-charts** (Charts)
- **Spring Data JPA / Hibernate** (ORM)

## 4. Tech Stack
- **Backend:** Java 17, Spring Boot 3.1.5, Spring Data JPA, Hibernate, PostgreSQL
- **Frontend:** Angular 17, TypeScript, Chart.js, ng2-charts, Angular HttpClient, Angular Router
- **Build:** Maven (with frontend-maven-plugin for Angular build)

## 5. Build & Run Steps
### Step 1: Create Database
```bash
psql -U postgres -c "CREATE DATABASE netly_app;"
```

### Step 2: Build & Run
```bash
# Run the automated setup
./setup.sh         # macOS/Linux
setup.bat          # Windows

# Start the application
cd portfolio-backend
mvn spring-boot:run
```

### Step 3: Open Browser
Go to: http://localhost:8080

**Result:** Both frontend and backend are served from port 8080.

#### Production Build & Run
```bash
cd portfolio-backend
mvn clean install -DskipTests
java -jar target/portfolio-app-1.0.0.jar
```

## 6. Configuration
- **Database password:** Edit `portfolio-backend/src/main/resources/application.properties`:
  ```properties
  spring.datasource.password=YOUR_SECURE_PASSWORD
  ```
- **Change port:**
  ```properties
  server.port=9090
  ```
  Then access at: http://localhost:9090

## 7. Deployment
- **Build production JAR:**
  ```bash
  cd portfolio-backend
  mvn clean install -DskipTests
  ```
- **Deploy the JAR:**
  ```bash
  scp target/portfolio-app-1.0.0.jar user@yourserver.com:/opt/portfolio/
  ssh user@yourserver.com
  cd /opt/portfolio
  java -jar portfolio-app-1.0.0.jar
  ```
- **Run as background service:**
  ```bash
  nohup java -jar portfolio-app-1.0.0.jar > app.log 2>&1 &
  ```

## 8. Using the Application
- **Dashboard:** View total portfolio value, gain/loss, asset allocation, and breakdown by type.
- **Add Asset:** Use "Add New Asset" to input details for stocks, real estate, gold, etc.
- **View/Edit/Delete Assets:** Use "My Assets" to manage your portfolio.
- **Track Performance:** Dashboard auto-calculates gain/loss and allocation.

## 9. API Details
- **Base URL:** `http://localhost:8080/api/`
- **Example Endpoint:** `/api/assets` (list, add, update, delete assets)
- **Database Schema (Assets Table):**
  - `id`: Primary key
  - `name`: Asset name
  - `type`: Asset type (EQUITY, CASH, REAL_ESTATE, etc.)
  - `current_value`: Current market value
  - `purchase_price`: Purchase price per unit
  - `purchase_date`: Date of purchase
  - `quantity`: Number of units
  - `description`: Additional details
  - `location`: Location (for real estate)
  - `currency`: Currency code
  - `created_at`: Creation timestamp

> For more API endpoints, explore `/api/` in your browser or check the backend source code.

## 10. Troubleshooting & FAQ
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
- **Where is the frontend code?** In `portfolio-frontend/` (or `ui/` in this repo).
- **Can I run frontend separately?** Yes: `cd portfolio-frontend && ng serve --port 4200`
- **How do I update the frontend?** Make changes, then run `mvn clean install` in backend.

---

**Happy Investing! 💰📈**

