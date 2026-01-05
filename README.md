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
Go to: http://localhost:8082

**Result:** Both frontend and backend are served from port 8082.

#### Run
```bash
java -jar target/netly-x.x.x.jar
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
  nohup java -jar netly-x.x.x.jar > app.log 2>&1 &
  ```

## 7. Using the Application
- **Dashboard:** View total portfolio value, gain/loss, asset allocation, and breakdown by type.
- **Add Asset:** Use "Add New Asset" to input details for stocks, real estate, gold, etc.
- **Add Liabilities:** Use "Add New Liability" to input details for Home Loans, Car Loans, Personal Loans etc.
- **View/Edit/Delete Assets:** Use "My Assets" to manage your Assets.
- **View/Edit/Delete Liability:** Use "My Liabilities" to manage your Liabilities.
- **Track Performance:** Dashboard auto-calculates gain/loss and allocation.

## 8. Production Deployment Guide

This guide provides step-by-step instructions for deploying the Netly application to a production server.

### Prerequisites

- Ubuntu/Debian-based server with sudo access
- Java installed (for running the Spring Boot application)
- Nginx installed
- Certbot for SSL certificates
- Database server (PostgreSQL) configured

### Deployment Steps

1. **Navigate to the deployment directory:**
   ```bash
   cd /opt
   ```

2. **Create application directories:**
   ```bash
   mkdir app
   cd app
   mkdir netly
   cd netly
   mkdir config
   ```

3. **Copy configuration files:**
   - Copy your application configuration files (e.g., `application-prod.properties`) to the `/opt/app/netly/config/` directory.

4. **Execute database scripts:**
   - Run the database schema scripts (e.g., `schema.sql`) to set up the database.

5. **Copy systemd service file:**
   - Copy the `netly-app.service` file to `/etc/systemd/system/`.

6. **Enable and reload systemd:**
   ```bash
   sudo systemctl enable netly-app
   sudo systemctl daemon-reload
   ```

7. **Copy Nginx configuration file:**
   - Copy the `netly.famvest.online` file to `/etc/nginx/sites-available/`.

8. **Obtain SSL certificate:**
   ```bash
   sudo certbot --nginx -d netly.famvest.online
   ```

9. **Enable Nginx site:**
   ```bash
   sudo cp /etc/nginx/sites-available/netly.famvest.online /etc/nginx/sites-available/netly.famvest.online
   cd /etc/nginx/sites-enabled
   sudo ln -sf /etc/nginx/sites-available/netly.famvest.online netly.famvest.online
   ```

10. **Restart services:**
    - Start the Netly application: `sudo systemctl start netly-app`
    - Restart Nginx: `sudo systemctl restart nginx`

### Notes

- Ensure all file paths and permissions are correctly set.
- Update configuration files with production-specific settings (database URLs, secrets, etc.).
- Monitor logs in `/opt/app/netly/logs/` for any issues.

## 9. Troubleshooting & FAQ
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

