# Expense Tracker

A personal expense tracker using Spring Boot 4.1.1, Java 26, MongoDB, Thymeleaf, Spring Security, a REST API and Caffeine. Each account sees only its own expenses. The dashboard follows the supplied blue, two-column login reference.

## Requirements

- JDK **26** and Maven (or the included Maven wrapper). In IntelliJ choose project SDK 26 and language level 26. A JDK 26 compiler cannot build a project set to Java 27.
- MongoDB, local or Atlas. MongoDB Atlas must allow your deployment's network access and the database user must have permission to read/write `expense_db`.

## Run locally

Set `MONGODB_URI` outside the repository, with `/expense_db` in the URL (or set `MONGODB_DATABASE=expense_db`). The URI is required on Render. When unset locally the app uses `mongodb://localhost:27017/expense_db`.

```bash
export MONGODB_URI='mongodb://localhost:27017/expense_db'
./mvnw test
./mvnw spring-boot:run
```

Open `http://localhost:8080/register`, register a user, and log in. Passwords are stored with BCrypt. The REST endpoints use the browser session and CSRF token. Do not publish a real MongoDB URI or password in code, screenshots, documentation, or Git history. If a credential has been shared, rotate it.

## REST endpoints

| Method | Path | Action |
| --- | --- | --- |
| GET | `/api/expenses?start=2026-09-01&end=2026-10-01` | List entries in `[start,end)` |
| POST | `/api/expenses` | Create expense |
| GET | `/api/expenses/{id}` | Read your expense |
| PUT | `/api/expenses/{id}` | Update your expense |
| DELETE | `/api/expenses/{id}` | Delete your expense |
| GET | `/api/expenses/summary/monthly?month=2026-09` | Total and category breakdown for a calendar month |
| GET | `/api/expenses/summary/weekly?date=2026-09-23` | Total and breakdown for Monday through Sunday containing date |

POST and PUT body: `{"title":"Groceries","amount":25.50,"category":"Food","date":"2026-09-23","note":"Market"}`. Amount is positive with at most two decimal places. Every endpoint needs a logged-in user; writes also need a valid CSRF header from the dashboard's HTML meta tag. Records from other users return 404. The summaries use a short Caffeine cache invalidated by writes. There is no hardcoded account or admin password.

## Docker and Render

Build locally with `docker build -t expense-tracker .` and run with a private environment variable: `docker run --rm -p 8080:8080 --env-file .env expense-tracker`. The `.env` file is excluded from Git and Docker build context. Do not put your production credentials in a public shell history.

`render.yaml` defines a free Docker web service. Connect this repository through [Render Blueprint](https://dashboard.render.com/blueprint/new) and set `MONGODB_URI` in the Dashboard to a fresh Atlas connection URI. Keep `MONGODB_DATABASE=expense_db`. The app binds to Render's `PORT`, and `/actuator/health` is the health endpoint. Set Atlas network access to permit your Render deployment. The free Render tier may spin down between visits. Live deployment requires GitHub access from Render and a valid MongoDB credential supplied privately.
