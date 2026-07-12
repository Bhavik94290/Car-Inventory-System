# Car Dealership Inventory System

A full-stack car dealership inventory system built with **Java Spring Boot** (backend), **React** (frontend) and **MySQL** — developed with a **TDD (Test-Driven Development)** approach.

Shoppers can register, log in, browse and search cars, view full vehicle details, add them to a cart, and pay via **Razorpay** — with a downloadable **PDF receipt** afterward. Admins get a separate dark back-office dashboard to add, update, delete and restock vehicles, with photo uploads and live inventory stats. Forgot your password? A real 6-digit code gets emailed to you. An **AI chat assistant** (Claude) helps visitors search inventory and check their own orders. Purchasing (directly or via checkout) decrements stock; when quantity hits 0, the Purchase/Add-to-Cart controls are disabled and the API rejects further purchases.

### Contents
- [App tour](#app-tour) — what it looks like, screen by screen
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Running the backend](#running-the-backend) / [frontend](#running-the-frontend)
- [Deployment](#deployment) — Netlify + Render
- [API reference](#api-reference)
- [TDD approach](#tdd-approach-red--green--refactor)
- [Feature checklist](#feature-checklist)
- [My AI usage](#my-ai-usage)

---

## App tour

### 🔐 Create an account, log in, and recover a forgotten password
Registration always creates a regular **USER** account — there's no way to self-register as admin. Forgot your password? Enter your email, get a real 6-digit code by email, and set a new password on the same screen.

| Register | Log in |
|---|---|
| ![Register](docs/screenshots/register.png) | ![Login](docs/screenshots/login.png) |

| Request a code | Enter the code + new password |
|---|---|
| ![Forgot password](docs/screenshots/forgot-password.png) | ![Enter OTP](docs/screenshots/forgot-password-otp.png) |

The code really does arrive by email (Gmail SMTP), not just an on-screen shortcut:

![Password reset email](docs/screenshots/password-reset-email.png)

### 🚘 Browse the showroom and drill into a vehicle
Filter by make, model, category and price range, jump straight to a category with the quick-filter chips, or click any car for its own detail page with full specs.

| Showroom | Vehicle detail |
|---|---|
| ![Showroom](docs/screenshots/showroom.png) | ![Vehicle detail](docs/screenshots/vehicle-detail.png) |

Logged-out visitors can browse and view details freely, but see a clear **"Log in to add this to your cart"** prompt instead of cart controls — no silently-broken buttons.

### 🛒 Cart, checkout, and secure payment
Add vehicles to your cart, review the total, and pay through **Razorpay Checkout** (cards, netbanking, and more) — the payment signature is verified server-side before the order is marked paid.

| Cart | Checkout | Razorpay payment |
|---|---|---|
| ![Cart](docs/screenshots/cart.png) | ![Checkout](docs/screenshots/checkout.png) | ![Razorpay payment](docs/screenshots/razorpay-payment.png) |

![Payment successful](docs/screenshots/payment-success.png)

### 📦 Order history & PDF receipts
Every past order is listed with its status, and a one-click **PDF receipt** can be downloaded any time — for that order or straight off the payment-success screen.

| My Orders | PDF receipt |
|---|---|
| ![My Orders](docs/screenshots/orders.png) | ![PDF receipt](docs/screenshots/receipt-pdf.png) |

### 🛠️ Admin back office
A separate dark dashboard with live inventory stats, full vehicle CRUD with photo upload, and the ability for an existing admin to create additional admin accounts — public registration can never do that itself.

| Admin dashboard | Create another admin |
|---|---|
| ![Admin dashboard](docs/screenshots/admin-panel.png) | ![Create admin](docs/screenshots/admin-create-admin.png) |

![Admin inventory table](docs/screenshots/admin-inventory-table.png)

---

## Tech stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), Spring Mail (Gmail SMTP), Lombok, Validation, DevTools (auto-restart) |
| Database | MySQL 8 (H2 in-memory used for tests) |
| Payments | Razorpay (Orders API + HMAC-SHA256 payment signature verification) |
| AI       | Claude (Anthropic Java SDK) — tool-use chat assistant |
| Frontend | React 18, Vite, Axios, React Router, jsPDF (client-side receipt generation) |
| Testing  | JUnit 5, Mockito, AssertJ |

## Project structure

```
AI_Kata_Car_Dealership_Inventory_System/
├── backend/                     # Spring Boot API
│   ├── pom.xml
│   ├── .env.example             # copy to .env and fill in real secrets (git-ignored)
│   └── src/
│       ├── main/java/com/kata/dealership/
│       │   ├── entity/          # User, Vehicle, Role, Order, OrderItem, OrderStatus, Payment, PaymentStatus
│       │   ├── repository/      # Spring Data JPA repos (+ search query)
│       │   ├── dto/             # Request/response objects with validation
│       │   ├── security/        # JwtService, JwtAuthFilter, SecurityConfig
│       │   ├── service/         # AuthService, VehicleService, PaymentService, RazorpayService, ChatService, FileStorageService
│       │   ├── controller/      # AuthController, VehicleController, OrderController, ChatController
│       │   ├── config/          # WebConfig (serves uploaded vehicle images), VehicleDataSeeder (starter inventory)
│       │   ├── util/            # IdGenerator (generated String primary keys)
│       │   └── exception/       # Custom exceptions + global handler
│       └── test/java/com/kata/dealership/service/
│           ├── VehicleServiceTest.java   # TDD tests for inventory logic
│           └── AuthServiceTest.java      # TDD tests for register/login/forgot-password/reset-password
└── frontend/                    # React app (Vite)
    └── src/
        ├── api/client.js        # Axios instance, JWT interceptor
        ├── context/             # AuthContext, CartContext (syncs with live prices/stock)
        ├── components/          # Navbar, SearchBar, CategoryChips, VehicleCard, CarIllustration, ChatWidget, ErrorBoundary
        ├── utils/                # categoryStyle.js (shared category colors), receipt.js (PDF generation)
        └── pages/                # Login, Register, ForgotPassword (combined OTP + reset), Home, VehicleDetail,
                                   # AdminDashboard, CartPage, CheckoutPage, OrdersPage
```

---

## Running the backend

1. **Prerequisites:** Java 17+, Maven, MySQL 8 running locally.
2. Create the database (or let the app create it):
   ```sql
   CREATE DATABASE dealership_db;
   ```
3. Configure secrets. Easiest: copy `backend/.env.example` to `backend/.env` and fill in real values — it's loaded automatically and never committed (git-ignored):
   ```
   DB_USERNAME=root
   DB_PASSWORD=<your mysql password>
   JWT_SECRET=<a long random secret, 64+ chars>
   RAZORPAY_KEY_ID=<from https://dashboard.razorpay.com/app/keys>
   RAZORPAY_KEY_SECRET=<from the same page>
   ANTHROPIC_API_KEY=<from https://console.anthropic.com/settings/keys>
   MAIL_USERNAME=<your Gmail address, for password-reset OTP emails>
   MAIL_PASSWORD=<a Google App Password from https://myaccount.google.com/apppasswords>
   MAIL_FROM=<defaults to MAIL_USERNAME if unset>
   UPLOAD_DIR=uploads/vehicles   # optional, defaults shown
   ```
   (Plain shell environment variables work too, if you'd rather not use the `.env` file.)

   Checkout, payment verification, the chat assistant, and forgot-password emails won't work without valid Razorpay/Anthropic/Gmail credentials — everything else (browsing, auth, admin CRUD) works fine without them. Razorpay also enforces its own per-transaction amount limit on new/unactivated accounts (commonly ₹5,00,000) — the seed data is priced to stay well under that. Gmail SMTP requires 2-Step Verification enabled on the account before an App Password can be generated.
4. Start the API:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The API runs at `http://localhost:8080`. On first boot with an empty database, `VehicleDataSeeder` populates ~24 realistic starter vehicles automatically. Thanks to Spring Boot DevTools, the app auto-restarts whenever your IDE recompiles a changed class — no need to stop/start it manually.

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

## Deployment

The frontend and backend deploy to **different** platforms — Netlify only hosts static sites, it can't run a persistent Spring Boot process or a database. This repo is preconfigured (`netlify.toml`, `render.yaml`) for **Netlify (frontend) + Render (backend)**, plus a separately-hosted MySQL database since Render's own managed database product is Postgres, not MySQL.

1. **Database.** Provision MySQL on a host of your choice (e.g. Aiven, Railway, Clever Cloud) and note its connection details — host, port, database name, username, password.
2. **Backend (Render).** Push this repo to GitHub, then on Render: **New → Blueprint**, connect the repo — it reads `render.yaml` automatically. Fill in the requested environment variables:
   - `DB_URL` — full JDBC URL, e.g. `jdbc:mysql://<host>:<port>/<db>?useSSL=true`
   - `DB_USERNAME`, `DB_PASSWORD` — from step 1
   - `JWT_SECRET` — a long random string
   - `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `ANTHROPIC_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` — same values as your local `.env`
   - `CORS_ALLOWED_ORIGINS` — leave for now, set after step 3
   Deploy, then note the resulting `https://<your-service>.onrender.com` URL.
3. **Frontend (Netlify).** **Add new site → Import from Git**, select the repo — Netlify auto-detects `netlify.toml` (builds from `frontend/`, publishes `dist/`). Add one build environment variable: `VITE_API_BASE_URL = https://<your-render-url>/api`. Deploy, then note the resulting `https://<your-site>.netlify.app` URL.
4. **Close the loop.** Back on Render, set `CORS_ALLOWED_ORIGINS` to that Netlify URL (comma-separated if you want to keep local origins too) and redeploy.

**Known limitations of this setup:** Render's free web service disk is ephemeral, so admin-uploaded vehicle photos (`/uploads/vehicles`) won't survive a redeploy or restart — fine for a demo, but a real deployment would need S3/Cloudinary-style storage instead. The free tier also spins down after inactivity, so the first request after idling can take 30–60s to wake up.

---

## API reference

### Auth
| Method | Endpoint             | Access    | Body                              | Notes |
|--------|----------------------|-----------|------------------------------------|-------|
| POST   | `/api/auth/register` | Public    | `{name, email, password}`         | Always creates a **USER** account — there is no way to self-register as admin. Password needs 8+ chars, upper/lower/digit/symbol. Returns JWT. |
| POST   | `/api/auth/register-admin` | **ADMIN** | `{name, email, password}`   | Creates a new **ADMIN** account. Only an already-authenticated admin can call this (`Authorization: Bearer <adminToken>`). Returns `{name, email, role}` — no token, since it's not a login for the new account. |
| POST   | `/api/auth/login`    | Public    | `{email, password}`               | Returns JWT + user info. |
| POST   | `/api/auth/forgot-password` | Public | `{email}`                    | Always returns the same generic message (no email enumeration). Emails a 6-digit OTP via Gmail SMTP if the address is registered. |
| POST   | `/api/auth/reset-password`  | Public | `{email, otp, newPassword}`  | OTP is valid for 10 minutes and single-use. Returns **400** if the email/OTP pair is invalid or expired. |

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
| GET    | `/api/orders/mine`     | Logged in | The caller's past orders, newest first. Each can be downloaded as a PDF receipt from the frontend. |

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
  "price": 245000,
  "quantity": 3,
  "imageUrl": "http://localhost:8080/uploads/vehicles/img_...png",
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
- Auth: register always creates a USER and returns a token, `registerAdmin` (admin-only endpoint) creates an ADMIN account, duplicate email rejected, login works, wrong credentials rejected, forgot-password emails a 6-digit OTP without leaking whether the email exists, reset-password updates the password for a valid OTP and rejects wrong/unknown-email/expired OTPs.
- Vehicles: add, get all, search (with pagination, sorting and the in-stock-only filter), update, update-not-found, delete, delete-not-found.
- Inventory: purchase decrements quantity, purchase fails at 0 stock, restock increments quantity, restock rejects non-positive amounts.

**Known gap:** `PaymentService`, `RazorpayService`, `ChatService` and `FileStorageService` don't have unit tests yet — they were added after the TDD suite above and haven't been back-filled with tests. Contributions welcome.

---

## Feature checklist

- [x] Register / Login with JWT (password policy: 8+ chars, upper/lower/digit/symbol)
- [x] Forgot / reset password flow with a real 6-digit OTP emailed via Gmail SMTP
- [x] Roles: USER and ADMIN (Spring Security route rules + `@EnableMethodSecurity`). Public registration always creates a USER; only an existing admin can create another admin, via the Admin Panel's "Create Admin" form
- [x] View all cars, search/filter/sort/paginate by make / model / category / price range / in-stock-only, plus one-click category chips
- [x] Vehicle detail page — click any car in the showroom (or a chat assistant result) to see its full details and specs on its own page
- [x] Purchase directly, or add to cart and pay via Razorpay checkout (signature-verified). Cart/checkout require being logged in as a non-admin user
- [x] Downloadable PDF receipt after checkout, and for any past order
- [x] Order history per user
- [x] Admin: separate dark dashboard with live inventory stats (total vehicles, units in stock, out of stock, inventory value); add / update / delete / restock vehicles, with photo upload; no cart access
- [x] AI chat assistant (Claude) for vehicle search and order lookup
- [x] Site-wide dark theme with Material-inspired elevated cards
- [x] Persistent MySQL storage via Spring Data JPA
- [x] Validation + global exception handling with clean JSON errors
- [x] TDD unit tests with JUnit 5 + Mockito

## My AI Usage

**Which AI tools I used:** Claude (Anthropic), via the Claude Code CLI.

**How I used it:**
- **Scaffolding:** Asked Claude to generate the initial Maven `pom.xml` dependency set (Spring Web, Data JPA, Security, Validation, MySQL driver, jjwt) and the Vite/React project skeleton, then adjusted versions and config by hand.
- **Test generation:** Asked Claude to draft the Mockito/JUnit 5 test cases for `AuthServiceTest` and `VehicleServiceTest` from the plain-English business rules in the kata brief, before any service implementation existed. These tests were run and confirmed failing (RED) before writing the corresponding service, then implemented to make them pass (GREEN).
- **New features, TDD-first:** The forgot/reset-password flow was built the same way — failing tests committed first (verified failing against a clean build), then the `AuthService` implementation, then the controller/security wiring.
- **Feature review:** The cart/checkout/Razorpay payments, order history, image uploads, and Claude-powered chat assistant were already written locally. Claude Code reviewed that code (correctness, security, consistency with the rest of the codebase), found and fixed real bugs along the way — e.g. the showroom's default sort silently falling back to oldest-first instead of newest, a currency symbol rendering as garbage in the PDF receipt because the PDF library's font doesn't support the ₹ glyph, and Razorpay's raw JSON error dump leaking onto the checkout screen instead of a readable message.
- **UI/UX design:** Asked Claude to design and build the dark admin dashboard, the site-wide dark theme, category quick-filter chips, and the vehicle-card/showroom visual refresh, iterating based on screenshots.
- **Debugging & tooling:** Used Claude to diagnose a broken local Maven plugin cache and a local network TLS-interception issue blocking Maven/npm/GitHub access, add Spring Boot DevTools for auto-restart, and wire up a git-ignored `.env` file so secrets don't need to be re-entered every run.
- **Git history:** Used Claude Code to build and maintain a clean commit history — a genuine Red → Green → Refactor sequence for the core TDD suite (each Red commit verified against a real failing build, each Green against a real passing test run), short conventional-commit messages (`feat:` / `fix:` / `update:` / `chore:` / `docs:`) for everything after, and a later cleanup pass reasoning through a `git filter-branch` rewrite of already-pushed commits without disturbing shared history.

At the developer's explicit request, ongoing commits do not carry an AI co-author trailer.
