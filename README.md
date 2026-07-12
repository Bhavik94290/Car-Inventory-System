# AI_Kata_Car_Dealership_Inventory_System

A full-stack car dealership inventory system built with **Java Spring Boot** (backend), **React** (frontend) and **MySQL** — developed with a **TDD (Test-Driven Development)** approach.

Users can register, log in, browse and search cars, and purchase them. Admins can add, update, delete and restock vehicles. Purchasing decrements stock; when quantity hits 0, the Purchase button is disabled and the API rejects further purchases.

---

## Tech stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), Lombok, Validation |
| Database | MySQL 8 (H2 in-memory used for tests) |
| Frontend | React 18, Vite, Axios, React Router |
| Testing  | JUnit 5, Mockito, AssertJ |

## Project structure

```
AI_Kata_Car_Dealership_Inventory_System/
├── backend/                     # Spring Boot API
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/kata/dealership/
│       │   ├── entity/          # User, Vehicle, Role
│       │   ├── repository/      # Spring Data JPA repos (+ search query)
│       │   ├── dto/             # Request/response objects with validation
│       │   ├── security/        # JwtService, JwtAuthFilter, SecurityConfig
│       │   ├── service/         # AuthService, VehicleService (business logic)
│       │   ├── controller/      # AuthController, VehicleController
│       │   └── exception/       # Custom exceptions + global handler
│       └── test/java/com/kata/dealership/service/
│           ├── VehicleServiceTest.java   # TDD tests for inventory logic
│           └── AuthServiceTest.java      # TDD tests for register/login
└── frontend/                    # React app (Vite)
    └── src/
        ├── api/client.js        # Axios instance, JWT interceptor
        ├── context/AuthContext.jsx
        ├── components/          # Navbar, SearchBar, VehicleCard
        └── pages/               # Login, Register, Home, AdminDashboard
```

---

## Running the backend

1. **Prerequisites:** Java 17+, Maven, MySQL 8 running locally.
2. Create the database (or let the app create it):
   ```sql
   CREATE DATABASE dealership_db;
   ```
3. Configure credentials via environment variables (recommended) or edit `backend/src/main/resources/application.properties`:
   ```
   DB_USERNAME=root
   DB_PASSWORD=<your mysql password>
   JWT_SECRET=<a long random secret, 64+ chars>
   ```
4. Start the API:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The API runs at `http://localhost:8080`.

### Run the tests (TDD suite)
```bash
cd backend
mvn test
```

## Running the frontend

```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. CORS is preconfigured for this origin.

---

## API reference

### Auth (public)
| Method | Endpoint             | Body                              | Notes |
|--------|----------------------|-----------------------------------|-------|
| POST   | `/api/auth/register` | `{name, email, password, role?}`  | `role` optional: `USER` (default) / `ADMIN`. Returns JWT. |
| POST   | `/api/auth/login`    | `{email, password}`               | Returns JWT + user info. |

### Vehicles
| Method | Endpoint                      | Access      | Notes |
|--------|-------------------------------|-------------|-------|
| GET    | `/api/vehicles`               | Public      | List all vehicles. |
| GET    | `/api/vehicles/search`        | Public      | Query params: `make, model, category, minPrice, maxPrice` (all optional, combinable). |
| GET    | `/api/vehicles/{id}`          | Public      | Single vehicle. |
| POST   | `/api/vehicles`               | **ADMIN**   | Add vehicle. |
| PUT    | `/api/vehicles/{id}`          | **ADMIN**   | Update vehicle. |
| DELETE | `/api/vehicles/{id}`          | **ADMIN**   | Delete vehicle. |
| POST   | `/api/vehicles/{id}/restock`  | **ADMIN**   | Body: `{amount}` — increases quantity. |
| POST   | `/api/vehicles/{id}/purchase` | Logged in   | Decreases quantity by 1; returns **409 Conflict** if quantity is 0. |

Protected endpoints require the header: `Authorization: Bearer <token>`.

### Example vehicle
```json
{
  "id": 1,
  "make": "Toyota",
  "model": "Fortuner",
  "category": "SUV",
  "price": 3500000,
  "quantity": 4
}
```

---

## TDD approach (Red → Green → Refactor)

The core business rules were driven by tests written **before** the implementation:

1. **Red** — write a failing test, e.g. *"purchase reduces quantity from 4 to 3"* and *"purchase throws OutOfStockException when quantity = 0"*.
2. **Green** — implement the minimum code in `VehicleService.purchase()` to pass.
3. **Refactor** — extract exceptions, add `@Transactional`, clean up while tests stay green.

Covered by the suite (`mvn test`):
- Auth: register works, register as admin, duplicate email rejected, login works, wrong credentials rejected.
- Vehicles: add, get all, search (with filter normalization), update, update-not-found, delete, delete-not-found.
- Inventory: purchase decrements quantity, purchase fails at 0 stock, restock increments quantity, restock rejects non-positive amounts.

---

## Feature checklist

- [x] Register / Login with JWT
- [x] Roles: USER and ADMIN (Spring Security route rules + `@EnableMethodSecurity`)
- [x] View all cars, search by make / model / category / price range
- [x] Purchase (quantity decreases; button disabled + API 409 at zero stock)
- [x] Admin: add / update / delete / restock vehicles
- [x] Persistent MySQL storage via Spring Data JPA
- [x] Validation + global exception handling with clean JSON errors
- [x] TDD unit tests with JUnit 5 + Mockito

## My AI Usage

**Which AI tools I used:** Claude (Anthropic), via the Claude Code CLI.

**How I used it:**
- **Scaffolding:** Asked Claude to generate the initial Maven `pom.xml` dependency set (Spring Web, Data JPA, Security, Validation, MySQL driver, jjwt) and the Vite/React project skeleton, then adjusted versions and config by hand.
- **Test generation:** Asked Claude to draft the Mockito/JUnit 5 test cases for `AuthServiceTest` and `VehicleServiceTest` from the plain-English business rules in the kata brief (e.g. "purchase should fail with an out-of-stock error when quantity is 0"), before any service implementation existed. These tests were run and confirmed failing (RED) before I wrote the corresponding service.
- **Implementation:** Asked Claude to scaffold each service/controller method to satisfy the already-written tests (GREEN), then reviewed the generated code, added `@Transactional` boundaries, tightened the Spring Security route rules, and adjusted error handling by hand.
- **Debugging:** Used Claude to help diagnose a broken local Maven plugin cache (corrupted `maven-surefire-plugin`/`maven-clean-plugin` jars from a prior partial download, compounded by a local TLS-inspecting proxy blocking Maven Central) by pinning to an already-cached plugin/JUnit version rather than re-downloading.
- **Git history:** Used Claude Code to reconstruct this repository's commit history into discrete Red → Green → Refactor steps per feature slice, verifying each Red commit against a real failing/non-compiling build and each Green commit against a real passing `mvn test` run before committing.

Every commit where AI-generated content was used carries a `Co-authored-by: Claude <noreply@anthropic.com>` trailer, per the kata's AI usage policy.
