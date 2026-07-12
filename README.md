# AI_Kata_Car_Dealership_Inventory_System

A full-stack car dealership inventory system built with **Java Spring Boot** (backend), **React** (frontend) and **MySQL** — developed with a **TDD (Test-Driven Development)** approach.

Users can register, log in, browse and search cars, add them to a cart, and pay via **Razorpay**. Admins can add, update, delete and restock vehicles, with photo uploads. An **AI chat assistant** (Claude) helps visitors search inventory and check their own orders. Purchasing (directly or via checkout) decrements stock; when quantity hits 0, the Purchase/Add-to-Cart controls are disabled and the API rejects further purchases.

---

## Tech stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), Lombok, Validation |
| Database | MySQL 8 (H2 in-memory used for tests) |
| Payments | Razorpay (Orders API + HMAC-SHA256 payment signature verification) |
| AI       | Claude (Anthropic Java SDK) — tool-use chat assistant |
| Frontend | React 18, Vite, Axios, React Router |
| Testing  | JUnit 5, Mockito, AssertJ |

## Project structure

```
AI_Kata_Car_Dealership_Inventory_System/
├── backend/                     # Spring Boot API
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/kata/dealership/
│       │   ├── entity/          # User, Vehicle, Role, Order, OrderItem, OrderStatus, Payment, PaymentStatus
│       │   ├── repository/      # Spring Data JPA repos (+ search query)
│       │   ├── dto/             # Request/response objects with validation
│       │   ├── security/        # JwtService, JwtAuthFilter, SecurityConfig
│       │   ├── service/         # AuthService, VehicleService, PaymentService, RazorpayService, ChatService, FileStorageService
│       │   ├── controller/      # AuthController, VehicleController, OrderController, ChatController
│       │   ├── config/          # WebConfig (serves uploaded vehicle images)
│       │   ├── util/            # IdGenerator (generated String primary keys)
│       │   └── exception/       # Custom exceptions + global handler
│       └── test/java/com/kata/dealership/service/
│           ├── VehicleServiceTest.java   # TDD tests for inventory logic
│           └── AuthServiceTest.java      # TDD tests for register/login
└── frontend/                    # React app (Vite)
    └── src/
        ├── api/client.js        # Axios instance, JWT interceptor
        ├── context/             # AuthContext, CartContext
        ├── components/          # Navbar, SearchBar, VehicleCard, CarIllustration, ChatWidget, ErrorBoundary
        └── pages/                # Login, Register, Home, AdminDashboard, CartPage, CheckoutPage, OrdersPage
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
   RAZORPAY_KEY_ID=<from https://dashboard.razorpay.com/app/keys>
   RAZORPAY_KEY_SECRET=<from the same page>
   ANTHROPIC_API_KEY=<from https://console.anthropic.com/settings/keys>
   UPLOAD_DIR=uploads/vehicles   # optional, defaults shown
   ```
   Checkout, payment verification, and the chat assistant won't work without valid Razorpay/Anthropic keys — everything else (browsing, auth, admin CRUD) works fine without them.
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
| GET    | `/api/vehicles`               | Public      | List all vehicles (unpaginated). |
| GET    | `/api/vehicles/search`        | Public      | Query params: `make, model, category, minPrice, maxPrice, inStockOnly, page, size (max 100), sortBy (createdAt\|make\|model\|category\|price\|quantity), sortDir (asc\|desc)`. Returns a Spring `Page<Vehicle>`. |
| GET    | `/api/vehicles/{id}`          | Public      | Single vehicle. |
| POST   | `/api/vehicles`               | **ADMIN**   | Add vehicle. Body may include `imageUrl`. |
| PUT    | `/api/vehicles/{id}`          | **ADMIN**   | Update vehicle. |
| DELETE | `/api/vehicles/{id}`          | **ADMIN**   | Delete vehicle. |
| POST   | `/api/vehicles/upload-image`  | **ADMIN**   | Multipart `file` (jpg/jpeg/png/gif/webp, max 5MB). Returns `{imageUrl}`; served from `/uploads/vehicles/**`. |
| POST   | `/api/vehicles/{id}/restock`  | **ADMIN**   | Body: `{amount}` — increases quantity. |
| POST   | `/api/vehicles/{id}/purchase` | Logged in   | Decreases quantity by 1; returns **409 Conflict** if quantity is 0. |

### Orders & payments (Razorpay)
| Method | Endpoint                | Access    | Notes |
|--------|--------------------------|-----------|-------|
| POST   | `/api/orders/checkout`  | Logged in | Body: `{items: [{vehicleId, quantity}]}`. Validates stock, creates a Razorpay order, returns `{orderId, razorpayOrderId, amount, currency, keyId}` for the frontend to open Razorpay Checkout with. |
| POST   | `/api/orders/verify`   | Logged in | Body: `{orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature}`. Verifies the HMAC signature, decrements stock and marks the order PAID; idempotent if replayed. Returns **402** on signature mismatch. |
| GET    | `/api/orders/mine`     | Logged in | The caller's past orders, newest first. |

### Chat assistant
| Method | Endpoint     | Access | Notes |
|--------|--------------|--------|-------|
| POST   | `/api/chat`  | Public | Body: `{message, history: [{role, content}]}`. Works for anonymous visitors (vehicle search) and, with a valid token, logged-in users (order lookup too). Returns `{reply, vehicles: [...]}`. |

Protected endpoints require the header: `Authorization: Bearer <token>`.

### Example vehicle
```json
{
  "id": "vehicle_3f9c2b1e4a7d4c6e9b0a1f2d3c4b5a6e",
  "make": "Toyota",
  "model": "Fortuner",
  "category": "SUV",
  "price": 3500000,
  "quantity": 4,
  "imageUrl": "http://localhost:8080/uploads/vehicles/img_...jpg",
  "createdAt": "2026-07-12T05:30:00Z"
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
- Vehicles: add, get all, search (with pagination, sorting and the in-stock-only filter), update, update-not-found, delete, delete-not-found.
- Inventory: purchase decrements quantity, purchase fails at 0 stock, restock increments quantity, restock rejects non-positive amounts.

**Known gap:** `PaymentService`, `RazorpayService`, `ChatService` and `FileStorageService` don't have unit tests yet — they were added after the TDD suite above and haven't been back-filled with tests. Contributions welcome.

---

## Feature checklist

- [x] Register / Login with JWT (password policy: 8+ chars, upper/lower/digit/symbol)
- [x] Roles: USER and ADMIN (Spring Security route rules + `@EnableMethodSecurity`)
- [x] View all cars, search/filter/sort/paginate by make / model / category / price range / in-stock-only
- [x] Purchase directly, or add to cart and pay via Razorpay checkout (signature-verified)
- [x] Order history per user
- [x] Admin: add / update / delete / restock vehicles, with photo upload
- [x] AI chat assistant (Claude) for vehicle search and order lookup
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
- **Feature review and commit organization:** The cart/checkout/Razorpay payments, order history, image uploads, and Claude-powered chat assistant were already written locally. Claude Code reviewed that code (correctness, security — e.g. confirming the Razorpay signature check uses a constant-time comparison and that no API keys were hardcoded — and consistency with the rest of the codebase), found and fixed a real bug (the showroom's default "Newest listed" sort silently fell back to oldest-first because the frontend sent a `sortBy` value the backend doesn't whitelist), and split the changes into one commit per feature slice, verifying the backend still built and all tests passed after each commit. Those commits don't carry an AI co-author trailer, at the developer's instruction, since the feature code itself predates this review.

Commits generated by Claude carry a `Co-authored-by: Claude <noreply@anthropic.com>` trailer, per the kata's AI usage policy.
