# 🚘 Car Dealership Inventory System

A full-stack car dealership inventory system — built with **Java Spring Boot**, **React**, and **MySQL** using a **TDD (Test-Driven Development)** approach. Shoppers browse and buy vehicles with real Razorpay payments and email OTP password resets; admins manage inventory from a separate dashboard.

## Local URLs & Ports

| App | URL | Port |
|---|---|---|
| Frontend (Vite dev server) | http://localhost:5173 | `5173` |
| Backend API (Spring Boot) | http://localhost:8080 | `8080` (or `$PORT` if set — used by Render) |

---

## Installation Guide

### 1. Repository Setup

```bash
git clone https://github.com/Bhavik94290/Car-Inventory-System
cd Car-Inventory-System
```

### 2. Backend Configuration

```bash
cd backend
copy .env.example .env
```

Fill in `backend/.env` with real values (never commit this file — it's git-ignored):

| Variable | Purpose |
|---|---|
| `DB_URL` | Optional — full JDBC URL for hosted MySQL. Leave unset locally (defaults to `jdbc:mysql://localhost:3306/dealership_db`) |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | 64+ char random Base64 string used to sign login JWTs |
| `RAZORPAY_KEY_ID` | From [dashboard.razorpay.com/app/keys](https://dashboard.razorpay.com/app/keys) |
| `RAZORPAY_KEY_SECRET` | From the same Razorpay dashboard page |
| `ANTHROPIC_API_KEY` | From [console.anthropic.com/settings/keys](https://console.anthropic.com/settings/keys) — powers the chat assistant |
| `MAIL_USERNAME` | Gmail address used to send password-reset OTP emails |
| `MAIL_PASSWORD` | 16-character [Google App Password](https://myaccount.google.com/apppasswords) (not your normal Gmail password) |
| `MAIL_FROM` | Optional — defaults to `MAIL_USERNAME` if unset |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins allowed to call the API — defaults to `http://localhost:5173,http://localhost:3000` |
| `UPLOAD_DIR` | Optional — where vehicle photos are stored, defaults to `uploads/vehicles` |

Maven resolves dependencies automatically on first run — no separate install step.

### 3. Frontend Configuration

```bash
cd frontend
npm install
copy .env.example .env
```

| Variable | Purpose |
|---|---|
| `VITE_API_BASE_URL` | Base URL the frontend calls for the API — defaults to `http://localhost:8080/api` for local dev |

### 4. Database Setup

```sql
CREATE DATABASE dealership_db;
```
(Or let the app create it automatically on first boot — `createDatabaseIfNotExist=true` is set.)

---

## Development Environment

### Backend Server

```bash
cd backend
mvn spring-boot:run
```
*Server available at: [http://localhost:8080](http://localhost:8080)*

On first boot with an empty database, `VehicleDataSeeder` populates ~26 realistic starter vehicles across 7 categories automatically. Spring Boot DevTools auto-restarts the app whenever a class is recompiled.

### Frontend Development Server

```bash
cd frontend
npm run dev
```
*Application available at: [http://localhost:5173](http://localhost:5173)*

---

## Feature Tour

### 🔐 Register, log in, and recover a forgotten password

Public registration always creates a **USER** account — there's no self-service way to become admin (`AuthController.register` in [`backend/src/main/java/com/kata/dealership/controller/AuthController.java`](backend/src/main/java/com/kata/dealership/controller/AuthController.java), UI in [`frontend/src/pages/Register.jsx`](frontend/src/pages/Register.jsx)). The password field enforces 8+ characters with an uppercase letter, lowercase letter, digit and symbol before the **Register** button will even submit.

![Create account](docs/screenshots/register.png)

Logging in ([`frontend/src/pages/Login.jsx`](frontend/src/pages/Login.jsx)) returns a JWT that `AuthContext` stores and attaches to every subsequent API call.

![Welcome back / login](docs/screenshots/login.png)

Forgot your password? Enter your email on [`frontend/src/pages/ForgotPassword.jsx`](frontend/src/pages/ForgotPassword.jsx) — the backend (`AuthService.forgotPassword`) always returns the same generic response whether or not the account exists (no email enumeration), but if it does exist, `EmailService` sends a real 6-digit OTP over Gmail SMTP.

![Forgot your password](docs/screenshots/forgot-password.png)

Enter the code plus a new password (validated with the same complexity rules) and `AuthService.resetPassword` verifies the OTP is correct and not expired (10-minute window) before updating the hash.

![Enter your code](docs/screenshots/forgot-password-otp.png)

The actual email the user receives, sent through the Gmail account configured via `MAIL_USERNAME`/`MAIL_PASSWORD`:

![Password reset email](docs/screenshots/password-reset-email.png)

### 🚘 Browse the showroom and drill into a vehicle

[`frontend/src/pages/Home.jsx`](frontend/src/pages/Home.jsx) calls `GET /api/vehicles/search` (`VehicleController` → `VehicleService`) with filters for make, model, category, min/max price, an "in stock only" checkbox, and a sort dropdown (Newest listed, Price low→high/high→low, Most in stock, Make A–Z). Quick-filter chips (`CategoryChips.jsx`) jump straight to a category — Electric, Hatchback, Pickup, SUV, Sedan, Sports, Van.

![The showroom floor](docs/screenshots/showroom.png)

Logged-out visitors browsing the grid see a clear **"Log in to add this to your cart"** prompt instead of a broken button:

![Showroom, logged out](docs/screenshots/showroom-logged-out.png)

Once logged in, each `VehicleCard` gets a quantity stepper and an **Add to Cart** button:

![Showroom, logged in with Add to Cart](docs/screenshots/showroom-logged-in.png)

Clicking any car opens its own detail page ([`frontend/src/pages/VehicleDetail.jsx`](frontend/src/pages/VehicleDetail.jsx)) with make, model, category, available units and its own quantity/Add to Cart control.

![Vehicle detail](docs/screenshots/vehicle-detail.png)

### 🛒 Cart, checkout, and secure payment

`CartContext` holds the cart client-side; [`frontend/src/pages/CartPage.jsx`](frontend/src/pages/CartPage.jsx) lets you adjust quantity or remove a line before totalling the price.

![Your cart](docs/screenshots/cart.png)

[`frontend/src/pages/CheckoutPage.jsx`](frontend/src/pages/CheckoutPage.jsx) calls `POST /api/orders/checkout` (`OrderController` → `PaymentService` → `RazorpayService`), which creates a Razorpay order server-side before the **Pay with Razorpay** button is even clickable.

![Checkout](docs/screenshots/checkout.png)

Razorpay's own hosted checkout handles card/netbanking/wallet entry — the app never sees raw card details:

![Razorpay payment options](docs/screenshots/razorpay-payment-options.png)
![Razorpay netbanking](docs/screenshots/razorpay-netbanking.png)
![Razorpay processing](docs/screenshots/razorpay-processing.png)
![Razorpay confirming payment](docs/screenshots/razorpay-confirming.png)

After Razorpay redirects back, the frontend posts the payment signature to `POST /api/orders/verify`, which `PaymentService` checks with HMAC-SHA256 against the Razorpay secret **before** marking the order `PAID` — a forged client-side "success" can't fake a paid order.

![Payment successful](docs/screenshots/payment-success.png)

### 📦 Order history & PDF receipts

[`frontend/src/pages/OrdersPage.jsx`](frontend/src/pages/OrdersPage.jsx) calls `GET /api/orders/mine`, listing every past order with its paid status and a one-click **Download Receipt (PDF)** button generated client-side with `jspdf` ([`frontend/src/utils/receipt.js`](frontend/src/utils/receipt.js)).

![My orders](docs/screenshots/orders.png)

The generated receipt — order ID, payment ID, itemized vehicle/qty/price, and total:

![PDF receipt](docs/screenshots/receipt-pdf.png)

Razorpay's own success screen and receipt, shown mid-flow for reference:

![Razorpay payment successful](docs/screenshots/razorpay-payment-successful.png)

### 🛠️ Admin back office

A separate dark dashboard ([`frontend/src/pages/AdminDashboard.jsx`](frontend/src/pages/AdminDashboard.jsx)) with live inventory stats (total vehicles, units in stock, out-of-stock count, total inventory value) pulled from `VehicleService`.

![Admin dashboard](docs/screenshots/admin-panel.png)

**+ Create admin** reveals a form that calls `POST /api/auth/register-admin` — the *only* way to mint another ADMIN account, and it's itself admin-only, closing the loop on privilege escalation.

![Create admin account](docs/screenshots/admin-create-admin.png)

Below the stats, full vehicle CRUD: add a vehicle (with photo upload via `POST /api/vehicles/upload-image` → `FileStorageService`), and per-row **Edit**, **Restock**, and **Delete** actions.

![Admin inventory table](docs/screenshots/admin-inventory-table.png)

The sort dropdown is shared with the public showroom view, so admins browsing the storefront get the same Newest/Price/Stock/Make sorting:

![Sort dropdown](docs/screenshots/showroom-sort-dropdown.png)

---

## Deployment

The frontend and backend deploy to **different** platforms — Netlify only hosts static sites, it can't run a persistent Spring Boot process or a database. This repo is preconfigured (`netlify.toml`, `render.yaml`, `backend/Dockerfile`) for **Netlify (frontend) + Render (backend, via Docker)**, plus a separately-hosted MySQL database since Render's own managed database product is Postgres, not MySQL.

1. **Database** — provision MySQL on a host of your choice (e.g. Railway, Aiven, Clever Cloud) and note its connection details.
2. **Backend (Render)** — push to GitHub, then **New → Blueprint**, connect the repo — it reads `render.yaml` automatically. Fill in `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `ANTHROPIC_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` (leave `CORS_ALLOWED_ORIGINS` for step 4). Deploy and note the resulting URL.
3. **Frontend (Netlify)** — Import from Git, Netlify auto-detects `netlify.toml`. Add build env var `VITE_API_BASE_URL = https://<your-render-url>/api`. Deploy and note the resulting URL.
4. **Close the loop** — back on Render, set `CORS_ALLOWED_ORIGINS` to your Netlify URL and redeploy.

**Known limitations:** Render's free web service disk is ephemeral, so admin-uploaded vehicle photos won't survive a redeploy. The free tier also spins down after inactivity (first request after idling can take 30–60s).

---

## Quality Assurance

#### 🧱 TDD approach (Red → Green → Refactor)

The core business rules were driven by tests written **before** the implementation — e.g. *"purchase reduces quantity from 4 to 3"* and *"purchase throws OutOfStockException when quantity = 0"* were failing tests first, then `VehicleService.purchase()` was implemented to pass them, then refactored (extracted exceptions, added `@Transactional`) while staying green.

Covered by the suite:
- **Auth**: register always creates a USER, `registerAdmin` (admin-only) creates an ADMIN, duplicate email rejected, login works/rejects bad credentials, forgot-password emails a 6-digit OTP without leaking whether the email exists, reset-password accepts a valid OTP and rejects wrong/unknown-email/expired ones.
- **Vehicles**: add, get all, search (pagination/sorting/in-stock-only), update, delete, not-found cases.
- **Inventory**: purchase decrements quantity, fails at 0 stock; restock increments quantity, rejects non-positive amounts.

**Known gap:** `PaymentService`, `RazorpayService`, `ChatService` and `FileStorageService` don't have unit tests yet.

---

## Running Tests

### Backend Test Suite

```bash
cd backend
mvn test
```

**Test Summary:** 23 tests passed (100%), covering Auth and Vehicle/Inventory business logic — JUnit 5 + Mockito + AssertJ, H2 in-memory DB. Full breakdown (per-test results, JaCoCo coverage, timings): [TestReport.md](TestReport.md).

---

## 🏗️ Architecture Overview

* **Frontend (React 18 + Vite)** — Axios, React Router, jsPDF for client-side receipt generation, dark theme with Material-inspired elevated cards.
* **Backend (Java 17 + Spring Boot 3)** — Spring Web, Spring Data JPA, Spring Security + JWT, Spring Mail, layered architecture (controller → service → repository).
* **Database (MySQL)** — persistent storage via Spring Data JPA; H2 in-memory for tests.
* **Payments (Razorpay)** — Orders API + HMAC-SHA256 signature verification server-side.
* **Email (Gmail SMTP)** — real 6-digit OTP codes for password reset.
* **AI (Claude, Anthropic Java SDK)** — tool-use chat assistant for vehicle search and order lookup.
* **Testing (JUnit 5 + Mockito + AssertJ)** — TDD unit test suite.
* **Deployment (Netlify + Render + Docker)** — static frontend on Netlify, containerized backend on Render.

---

## API Documentation

### 🔑 Auth Endpoints

| Method | Endpoint                    | Access    | Description |
|--------|------------------------------|-----------|--------------|
| POST   | `/api/auth/register`        | Public    | Register — always creates a USER account |
| POST   | `/api/auth/register-admin`  | Admin only | Create another admin account |
| POST   | `/api/auth/login`           | Public    | Login, returns JWT |
| POST   | `/api/auth/forgot-password` | Public    | Emails a 6-digit OTP if the account exists |
| POST   | `/api/auth/reset-password`  | Public    | Verifies `{email, otp, newPassword}`, OTP valid 10 min |

### 🚗 Vehicle Endpoints

| Method | Endpoint                       | Access     | Description |
|--------|--------------------------------|------------|--------------|
| GET    | `/api/vehicles`                | Public     | List all vehicles |
| GET    | `/api/vehicles/search`         | Public     | Filter/sort/paginate |
| GET    | `/api/vehicles/{id}`           | Public     | Single vehicle |
| POST   | `/api/vehicles`                | Admin only | Add vehicle |
| PUT    | `/api/vehicles/{id}`           | Admin only | Update vehicle |
| DELETE | `/api/vehicles/{id}`           | Admin only | Delete vehicle |
| POST   | `/api/vehicles/upload-image`   | Admin only | Upload a vehicle photo |
| POST   | `/api/vehicles/{id}/restock`   | Admin only | Increase quantity |
| POST   | `/api/vehicles/{id}/purchase`  | Logged in  | One-click purchase (decrements stock) |

### 💳 Orders & Payments

| Method | Endpoint               | Access    | Description |
|--------|-------------------------|-----------|--------------|
| POST   | `/api/orders/checkout` | Logged in | Creates a Razorpay order from the cart |
| POST   | `/api/orders/verify`   | Logged in | Verifies payment signature, marks order PAID |
| GET    | `/api/orders/mine`     | Logged in | Caller's past orders |

### 💬 Chat Assistant

| Method | Endpoint    | Access | Description |
|--------|-------------|--------|--------------|
| POST   | `/api/chat` | Public | AI-powered vehicle search & order lookup |

A full [Postman collection](postman_collection.json) with every request pre-built is included in the repo root.

---

## AI Tools Used

- **Claude (Anthropic), via Claude Code CLI** — the primary tool used throughout this project.
- **Scaffolding & TDD** — generated the initial Maven/Vite project skeletons, then drafted failing Mockito/JUnit 5 tests from the kata's plain-English business rules *before* any service implementation existed (verified RED against a real build), followed by the implementation to turn them GREEN.
- **Feature review** — reviewed already-written code (cart/checkout/Razorpay, image uploads, chat assistant) for correctness and security, catching real bugs (default sort silently falling back to oldest-first, a currency glyph the PDF font couldn't render, a raw Razorpay error leaking onto the checkout screen).
- **UI/UX** — designed the dark admin dashboard, site-wide theme, category chips, and the Material-inspired card redesign, iterating from screenshots.
- **New features, TDD-first** — the forgot/reset-password OTP flow (with real Gmail SMTP delivery) and the admin-registration lockdown were both built RED → GREEN → refactor.
- **Debugging & tooling** — diagnosed a local Maven/git TLS-interception issue, added Spring Boot DevTools auto-restart, wired up a git-ignored `.env` file, and set up the Netlify/Render deployment pipeline.
- **Git history** — maintained a clean, conventional-commit history (`feat:` / `fix:` / `docs:`) with a genuine Red → Green → Refactor sequence for the core TDD suite.

At the developer's explicit request, ongoing commits do not carry an AI co-author trailer.
