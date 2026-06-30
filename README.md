# Smart Personal Finance Platform

A hybrid personal finance management application combining a Spring Boot REST API backend with a JavaFX desktop client. Track income and expenses, set budgets and savings goals, get spending insights, and export financial reports — all from a single desktop app.

## Features

- **Income & Expense Tracking** — Log transactions with categories, payment methods, currencies, and recurring schedules
- **Budget Planner** — Set budgets per category with configurable strategies (Aggressive / Conservative)
- **Goals & Notifications** — Track savings goals with alerts when budgets are exceeded
- **Spending Insights** — Automated analysis including savings rate, top spending categories, and spending diversity
- **Report Export** — Generate PDF and CSV reports of financial activity
- **JavaFX Desktop UI** — Login screen followed by a tabbed dashboard (Income & Expenses, Budget Planner, Goals & Notifications, Spending Insights)

## Architecture

This isn't just CRUD — it applies classic OOP design patterns throughout:

- **Singleton** — `SystemConfig` for global app configuration
- **Factory** — `TransactionFactory` for constructing transaction objects
- **Observer** — `BudgetAlertObserver` / `Subject` / `Observer` for budget-exceeded notifications
- **Strategy** — `AggressiveBudgetStrategy` / `ConservativeBudgetStrategy` for interchangeable budgeting logic

Layered structure: `controller` → `service` → `repository` → `model`, with a separate `ui` package for the JavaFX views.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2.5, Spring Web, Spring Data JPA
- **Database:** MySQL
- **Desktop UI:** JavaFX 21
- **PDF Generation:** iText
- **CSV Export:** Apache Commons CSV
- **Build Tool:** Maven
- **Other:** Lombok

## Installation

1. Clone the repository
   ```bash
   git clone https://github.com/spandhana-2128/smart-personal-finance-platform.git
   cd smart-personal-finance-platform
   ```

2. Create a MySQL database
   ```sql
   CREATE DATABASE finance_db;
   ```

3. Configure your database credentials 

4. Build and run with Maven
   ```bash
   ./mvnw spring-boot:run
   ```
   This starts the Spring Boot backend on port `8080` and then launches the JavaFX desktop client automatically.





## License

Specify your license here (e.g. MIT).
