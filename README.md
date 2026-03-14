# Travel Management System (JSP + Servlet + JDBC + MySQL)

This project is now wired to a Java web backend using:
- JSP (views)
- Java Servlets (controllers)
- JDBC (data access)
- MySQL (database)

## Tech Stack
- Java 17
- Maven (WAR project)
- Jakarta Servlet 6
- JSTL
- MySQL Connector/J

## Project Structure
- `src/main/java/com/travel/db/DBConnection.java` - JDBC connection helper
- `src/main/java/com/travel/dao/TravelDao.java` - SQL queries and insert operations
- `src/main/java/com/travel/servlet/*` - page and form servlets
- `src/main/webapp/WEB-INF/views/*.jsp` - JSP pages
- `src/main/webapp/assets/css/style.css` - UI styling
- `src/main/resources/db.properties` - database connection config
- `sql/travel.sql` - schema script

## Database Setup
1. Start MySQL server.
2. Execute `sql/travel.sql`.
3. Execute `sql/seed-data.sql` for demo data.
4. Update `src/main/resources/db.properties` if needed:

```properties
db.url=jdbc:mysql://localhost:3306/travel_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
db.username=root
db.password=admin
```

## Build
```bash
mvn clean package
```

WAR file output:
- `target/travel-management-system.war`

## Run
Deploy the WAR on a Jakarta-compatible server (Tomcat 10.1+ recommended), then open:
- `http://localhost:8080/travel-management-system/dashboard`

## Features Connected to MySQL
- Dashboard KPIs from users/packages/bookings/payments/reviews tables
- Packages listing with search/filter/sort
- Booking creation with transaction-safe payment insert
- Bookings table view
- Payments table view
- Reviews list + add review form
- Data hub for users/destinations/hotels/transport
