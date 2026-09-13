# Online Reservation System

A complete Java Swing + JDBC + SQLite train/transport reservation system.

## Features
- Login with username/password
- Train number lookup and automatic train-name population
- Reservation/booking form
- Automatic unique PNR generation
- Booking confirmation dialog
- Cancellation by PNR
- Fetch booking details before cancellation
- Confirmation before deleting a booking
- Input validation
- SQLite database created automatically
- Sample users and trains inserted automatically

## Requirements
- JDK 17+
- Maven 3.8+

## Demo login
Username: `admin`
Password: `admin123`

Another account:
Username: `user`
Password: `user123`

## Run in VS Code / terminal

Open the project folder and run:

```powershell
mvn clean compile
mvn exec:java
```

The first run downloads the SQLite JDBC driver automatically.

## Database
A file named `reservation.db` is created automatically in the project folder.

Tables:
- users
- trains
- reservations

## Main source files
- `Main.java` - starts the application
- `Database.java` - SQLite connection and table creation
- `DatabaseInitializer.java` - sample data
- `LoginFrame.java` - login screen
- `DashboardFrame.java` - main menu
- `ReservationFrame.java` - booking screen
- `CancellationFrame.java` - cancellation screen
- `Reservation.java` - reservation model
- `Train.java` - train model
- `ReservationDAO.java` - reservation database operations
- `TrainDAO.java` - train lookup
- `UserDAO.java` - login verification
- `Validation.java` - input validation
- `PNRGenerator.java` - PNR generation

## Notes
This is an academic/demo project. For a production system, passwords should be hashed and stronger authorization/security should be added.
