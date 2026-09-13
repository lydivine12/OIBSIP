# Digital Library Management System

Spring Boot (Java) web app with separate Admin and User roles, book
catalogue management, issuing/returns, automatic overdue fines, and
advance booking (reservations) for books that are currently checked out.

> **Note on this build**: this sandbox has no access to Maven Central, so
> `mvn` couldn't be run here to compile/test it. The code was written and
> reviewed carefully, but please run `mvn spring-boot:run` locally as your
> first step and report back if anything doesn't compile cleanly.

## Tech stack
- Java 17, Spring Boot 3.3 (Web MVC + Spring Data JPA + Thymeleaf)
- H2 file-based database (zero setup — see "Switching databases" to use MySQL)
- Session-based auth with BCrypt-hashed passwords (no Spring Security dependency, kept intentionally lightweight)

## Run it
```
mvn spring-boot:run
```
Then open **http://localhost:8080**

Demo logins (seeded automatically on first run):
| Role  | Email             | Password |
|-------|-------------------|----------|
| Admin | admin@library.com | admin123 |
| User  | user@library.com  | user123  |

You can also register new user accounts from the login page.

## Feature -> code map

### Admin module
| Feature | Where |
|---|---|
| Admin login | `AuthController` + `AuthInterceptor` (role check) |
| Add/edit/delete books | `AdminController` (`/admin/books/*`) + `admin/books.html` |
| View issued books & due dates | `AdminController#issued` + `admin/issued.html` |
| Manage member accounts | `AdminController#members` + `admin/members.html` |
| Mark fines paid | `AdminController#markFinePaid` + `admin/fines.html` |

### User module
| Feature | Where |
|---|---|
| Registration & login | `AuthController` (`/register`, `/login`) |
| Browse catalogue by category | `UserController#catalogue` + `user/catalogue.html` |
| Search by title/author | `BookRepository#findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase` |
| Issue a book | `IssueService#issueBook` — decrements `availableQuantity`, sets a 14-day due date |
| Return a book | `IssueService#returnBook` — increments availability *or* auto-fulfils the oldest pending reservation |
| Overdue fine (₹5/day) | `IssueService#returnBook` — computed automatically on return, `IssueService.FINE_PER_DAY` |
| Advance booking (reserve an issued book) | `ReservationService#reserve` — only allowed when `availableQuantity == 0` |
| Contact/query form | `UserController#contactSubmit` -> `ContactService` -> visible to admin at `/admin/messages` |

## Data model
`Member` (role ADMIN/USER) · `Book` · `IssueRecord` (issue/due/return dates)
· `Fine` (linked 1:1 to an `IssueRecord`) · `Reservation` (PENDING /
FULFILLED / CANCELLED) · `ContactMessage`.

## Switching databases
The brief mentions MySQL or SQLite. H2 (file-based) is used by default so
the project runs with zero setup. To use **MySQL**:
1. In `pom.xml`, uncomment the `mysql-connector-j` dependency.
2. In `application.properties`, swap the datasource block for the
   commented-out MySQL block at the bottom of the file.

SQLite is possible too but needs the community Hibernate SQLite dialect
(`community-dialects` starter) since it isn't a first-class Hibernate
dialect — ask if you'd like that wired up instead of MySQL.

## Project layout
```
src/main/java/com/library/
  model/        JPA entities
  repository/   Spring Data JPA repositories
  service/      Business logic (issuing, fines, reservations, etc.)
  controller/   MVC controllers (Auth, Home, Admin, User)
  config/       Session auth interceptor + demo data seeding
src/main/resources/
  templates/    Thymeleaf HTML (login, register, user/*, admin/*)
  static/css/   Stylesheet
  application.properties
```
