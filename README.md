# SportGrid - Turf Booking System

Java Swing front end, plain Java backend (JDBC), MySQL database.

## Setup (every team member, once)

1. Install **JDK 17+**, **MySQL 8+** and **IntelliJ IDEA Community**.
2. Create the database: open MySQL Workbench, open `sql/schema.sql`, run it (lightning icon).
   Or from a terminal: `mysql -u root -p < sql/schema.sql`
3. Copy `db.properties.example` to `db.properties` and put your own MySQL password in it.
   (`db.properties` is git-ignored. Never commit passwords.)
4. In IntelliJ: **File > Open** the `SportGrid` folder.
   - Right-click `src` > **Mark Directory as > Sources Root**.
   - **File > Project Structure > Libraries > + > From Maven**, search `com.mysql:mysql-connector-j:8.4.0`, OK.
5. Run `src/main/Main.java`. It runs a console demo of the whole booking flow and prints what happened.
   If you see "All backend checks finished." the database and backend work.

Run from a terminal instead (Linux/Mac; on Windows use `;` instead of `:` in the classpath):

    javac -cp "lib/*" -d out $(find src -name "*.java")
    java  -cp "out:lib/*" main.Main

(put the connector jar in a `lib/` folder first)

## Layers (who calls whom)

    ui  ->  service  ->  dao  ->  db (MySQL)
                 \-> model (plain objects used everywhere)

| Package | Job |
|---|---|
| `model` | Plain classes: `User` (abstract) with `Player`, `TurfOwner`, `Admin`; `Turf`, `TimeSlot`, `Booking`; `Payment` (abstract) with `UPIPayment`, `CardPayment`; `Review`; enums |
| `dao` | The only code that writes SQL. One DAO per table |
| `service` | The rules: validation, who may do what, booking transaction |
| `db` | `DBConnection` (reads `db.properties`) and the exception types |
| `ui` | Swing screens. **Call only `service` classes, never `dao` or SQL** |

## Cheat sheet for the UI team

Create the services once and reuse them:

    AuthService auth = new AuthService();
    BookingService bookings = new BookingService();
    TurfService turfs = new TurfService();
    AdminService admin = new AdminService();

| Screen action | Call |
|---|---|
| Sign up | `auth.register(name, email, phone, password, Role.PLAYER or Role.OWNER)` |
| Log in | `User u = auth.login(email, password)`, then check `u.getRole()` to open the right screen |
| Search | `bookings.searchTurfs(sport, location)` (blank = no filter) |
| Free slots | `bookings.getAvailableSlots(turfId)` |
| Book + pay | `bookings.bookSlot(player, slotId, new UPIPayment("name@bank"))` or `new CardPayment(holder, number, "MM/yy")` |
| Cancel | `bookings.cancelBooking(player, bookingId)` |
| My bookings | `bookings.getBookingHistory(player)` |
| Owner: add turf | `turfs.addTurf(owner, name, location, sport, price)` (needs admin approval) |
| Owner: add slots | `turfs.generateHourlySlots(owner, turfId, date, 6, 22)` or `turfs.addSlot(...)` |
| Owner: dashboard | `turfs.loadOwnerDashboard(owner)`, `turfs.getOwnerBookings(owner)` |
| Review | `turfs.addReview(player, turfId, rating, comment)`, `turfs.getReviews(turfId)` |
| Admin | `admin.getPendingTurfs(u)`, `approveTurf`, `rejectTurf`, `listUsers`, `removeUser`, `removeReview` |

**Error handling in the UI:** the services throw exceptions with a message that is safe to show to the user.

    try {
        bookings.bookSlot(player, slotId, payment);
    } catch (IllegalArgumentException | IllegalStateException | PaymentFailedException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());          // expected problems
    } catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, "Something went wrong. Please try again.");  // DB down etc.
    }

Swing tip: run database calls off the UI thread (`SwingWorker`) for slow screens, otherwise the window freezes.

## OOP concepts in the code (for the report and viva)

- **Encapsulation**: every field is `private`; changes go through getters/setters or rule-checking methods such as `TimeSlot.book()`.
- **Inheritance**: `Player`, `TurfOwner`, `Admin` extend `User`; `UPIPayment`, `CardPayment` extend `Payment`.
- **Polymorphism**: `getDashboard()` differs per user type; `processPayment()` differs per payment type, and `BookingService` just calls it.
- **Abstraction**: `User` and `Payment` are abstract and force subclasses to implement `getDashboard()` / `processPayment()`.

## Good to know

- **Payments are simulated.** `processPayment()` only validates the details (UPI format, card checksum + expiry). No money moves. Full card numbers are never stored, only the last 4 digits.
- **Double booking is prevented** by one atomic SQL update inside a transaction (`TimeSlotDAO.markBooked`). Two people clicking the same slot at once: exactly one wins.
- **Passwords** are salted and hashed with PBKDF2. The demo admin is `admin@sportgrid.com` / `Admin@123`: change it before showing anything outside the college.
- Test card for the demo: `4111 1111 1111 1111`, any future expiry (e.g. `12/30`).
