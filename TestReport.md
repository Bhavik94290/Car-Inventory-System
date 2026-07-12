# Car Dealership Inventory System - Test Report

**Generated on:** July 12, 2026
**Test Framework:** JUnit 5 + Mockito + AssertJ (Maven Surefire, H2 in-memory DB)
**Total Test Cases:** 23
**Test Result:** ALL TESTS PASSED

Run it yourself:

```bash
cd backend
mvn test
```

An HTML coverage report (via JaCoCo) is generated at `backend/target/site/jacoco/index.html` on every `mvn test` run.

## Test Summary

| Metric | Result |
| --- | --- |
| **Test Classes** | 2 passed, 2 total |
| **Test Cases** | 23 passed, 23 total |
| **Failures / Errors** | 0 / 0 |
| **Execution Time** | 2.67 s (test bodies) / 5.77 s (full `mvn test` incl. Spring context & JVM startup) |
| **Overall Status** | PASSED |

## Code Coverage Report

Coverage is scoped to the layers this kata's TDD suite targets — the **service layer's business rules** — via JaCoCo instrumentation. Controllers, security, config and entities are exercised manually (see [README > Feature Tour](README.md#feature-tour)) rather than by unit test, which shows up as 0% below; that's an intentional, documented scope, not an oversight.

| Class (unit-tested) | % Lines | % Branches | % Methods |
| --- | --- | --- | --- |
| `service/AuthService.java` | 100% (57/57) | 87.5% (7/8) | 92.9% (13/14) |
| `service/VehicleService.java` | 100% (40/40) | 100% (8/8) | 100% (11/11) |

| Package (whole tree) | % Lines | % Branches | % Methods |
| --- | --- | --- | --- |
| **All `com.kata.dealership.*`** | **22.5%** (163/725) | **12.8%** (15/117) | **26.1%** (149/571) |
| `service` (10 classes, 2 tested) | 25.1% (97/386) | 17.6% (15/85) | 34.8% (24/69)* |
| `controller` | 0% (0/40) | 0% (0/10) | 0% (0/23) |
| `security` | 0% (0/76) | 0% (0/14) | 0% (0/21) |
| `config` | 0% (0/24) | 0% (0/4) | 0% (0/6) |
| `entity` (Lombok builders/getters) | 31.7% (20/63) | — | 30.4% (45/148) |
| `dto` (Lombok builders/getters) | 40.2% (37/92) | — | 27.1% (75/277) |
| `exception` | 20.0% (8/40) | — | 16.7% (4/24) |
| `util` | 100% (1/1) | — | 100% (1/1) |

<sup>*`service`-level method % looks lower than the two 100%-covered classes because it's averaged across all 10 classes in the package, 8 of which (`PaymentService`, `RazorpayService`, `ChatService`, `EmailService`, `FileStorageService`, etc.) have no unit tests yet — see [Known Gap](#known-gap) below.</sup>

**Known gap:** `PaymentService`, `RazorpayService`, `ChatService`, `EmailService` and `FileStorageService` don't have unit tests yet (same gap the README already calls out) — their Razorpay/SMTP/filesystem/Anthropic-SDK side effects were verified manually end-to-end instead (see the screenshots in the README's Feature Tour).

---

## Detailed Test Results

### Test Classes & Features

* **`AuthServiceTest`** (11 tests) — registration, admin creation, login, and the forgot/reset-password OTP flow.
* **`VehicleServiceTest`** (12 tests) — vehicle CRUD, search/filtering, and inventory purchase/restock rules.

### AuthServiceTest — 11/11 passed

| # | Test | What it proves |
| --- | --- | --- |
| 1 | `register_createsUserAndReturnsToken` | Public registration always creates a **USER**, hashes the password, and returns a JWT |
| 2 | `registerAdmin_createsAdminAccount` | The admin-only path creates an **ADMIN** account, bypassing the public register flow |
| 3 | `register_duplicateEmail_throws` | Registering an already-used email is rejected |
| 4 | `login_success_returnsToken` | A valid user/password pair authenticates and returns a token |
| 5 | `login_badCredentials_throws` | Wrong credentials are rejected |
| 6 | `forgotPassword_existingEmail_emailsOtp` | A matching account gets a 6-digit OTP emailed to it |
| 7 | `forgotPassword_unknownEmail_doesNotSave` | An unknown email gets the same generic response — no OTP saved, no email sent, no enumeration leak |
| 8 | `resetPassword_validOtp_updatesPassword` | A valid OTP updates the password and clears the OTP |
| 9 | `resetPassword_wrongOtp_throws` | An incorrect OTP is rejected |
| 10 | `resetPassword_unknownEmail_throws` | Resetting for an unknown email is rejected |
| 11 | `resetPassword_expiredOtp_throws` | An OTP past its 10-minute window is rejected |

### VehicleServiceTest — 12/12 passed

| # | Test | What it proves |
| --- | --- | --- |
| 1 | `addVehicle_savesVehicle` | Adding a vehicle persists and returns it |
| 2 | `getAllVehicles_returnsList` | Listing returns everything in the repository |
| 3 | `search_delegatesToRepository` | Search filters are delegated to the repository, blank strings become `null` |
| 4 | `search_inStockOnly_passedThrough` | The "in stock only" flag is passed through to the repository |
| 5 | `updateVehicle_updatesFields` | Updating an existing vehicle changes its fields |
| 6 | `updateVehicle_notFound_throws` | Updating a non-existent vehicle throws `ResourceNotFoundException` |
| 7 | `deleteVehicle_deletes` | Deleting an existing vehicle removes it |
| 8 | `deleteVehicle_notFound_throws` | Deleting a non-existent vehicle throws `ResourceNotFoundException` |
| 9 | `purchase_reducesQuantity` | A purchase reduces quantity by 1 (e.g. 5 → 4) |
| 10 | `purchase_outOfStock_throws` | Purchasing at 0 quantity throws `OutOfStockException` |
| 11 | `restock_increasesQuantity` | Restocking increases quantity by the given amount |
| 12 | `restock_invalidAmount_throws` | Restocking with a zero/negative amount is rejected |

### Test Categories Summary

| Test Class | Tests Passed | Tests Failed | Success Rate |
| --- | --- | --- | --- |
| AuthServiceTest | 11 | 0 | 100% |
| VehicleServiceTest | 12 | 0 | 100% |
| **TOTAL** | **23** | **0** | **100%** |

---

## Test Quality Metrics

* **TDD approach:** every test above was written **Red** (failing, against no/stub implementation) before the corresponding `AuthService`/`VehicleService` method was implemented **Green**, then refactored (exceptions extracted, `@Transactional` added) while staying green.
* **Error handling coverage:**
  * Duplicate email rejected on registration
  * Bad login credentials rejected
  * Wrong / unknown-email / expired password-reset OTP rejected
  * Update/delete on a non-existent vehicle throws `ResourceNotFoundException`
  * Purchase at zero stock throws `OutOfStockException`
  * Restock with a non-positive amount rejected
* **No email-enumeration:** `forgotPassword` returns an identical response whether or not the email exists.
* **Data integrity:** stock quantity is verified to change correctly after both purchase and restock; updated vehicle fields are verified post-update.

---

## Performance Analysis

| Metric | Value | Status |
| --- | --- | --- |
| **Total test execution time** | 2.67 s | Good |
| **Slowest test (steady-state)** | `register_createsUserAndReturnsToken` — 177 ms (BCrypt password hashing) | Expected |
| **First-test class overhead** | `register_duplicateEmail_throws` — 2.21 s (BCrypt/Spring class-loading warm-up on first use, not test logic) | Expected, one-time |
| **Fastest test** | `restock_invalidAmount_throws` — 2 ms | Optimal |
| **Full `mvn test` (incl. JVM startup)** | 5.77 s | Acceptable |

---

## Conclusion

**Success criteria met:**
* All 23 test cases passed successfully (100%)
* The two classes under test (`AuthService`, `VehicleService`) reach 100% line coverage, ≥87.5% branch coverage
* Core business rules — auth, password reset, inventory purchase/restock — are TDD-driven and green
* Error handling and edge cases (duplicate email, bad credentials, expired OTP, out-of-stock, invalid restock, not-found) are all explicitly tested

**Known gap (unchanged from README):** `PaymentService`, `RazorpayService`, `ChatService`, `EmailService` and `FileStorageService` have no unit tests — their external integrations (Razorpay, Gmail SMTP, Anthropic Claude, filesystem uploads) were validated manually instead.

---

**Report status:** COMPLETE — ALL 23 TESTS PASSED
