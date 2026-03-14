# Travel Management System Tools Guide

This document explains the main tools and technologies used in the Travel Management System project. It focuses on what each tool does, why it is used here, and where it appears inside the codebase.

Project type: Java web application
Build output: WAR file
Main runtime flow: Browser -> Tomcat -> Servlet -> DAO -> JDBC -> MySQL -> JSP response

## 1. Java 17

Java 17 is the core programming language used for the backend of this project. All business logic, request handling, database access, and helper classes are written in Java.

- Why it is used:
  Java is stable, strongly typed, and a standard choice for servlet-based web applications.
- What it handles here:
  Servlets, DAO methods, database connection logic, redirects, and request forwarding.
- Where it appears:
  `src/main/java/com/travel/...`
- Important note:
  The Maven compiler in `pom.xml` is configured to compile the project with Java 17.

## 2. Maven

Maven is the build and dependency management tool used in this project.

- Why it is used:
  It keeps library versions organized and automates compiling, packaging, and generating the WAR file.
- What it handles here:
  Dependency download, Java compilation, resource copying, and WAR creation.
- Where it appears:
  `pom.xml`
- Important note:
  Running `mvn package` creates `target/travel-management-system.war`.

## 3. WAR Packaging

WAR stands for Web Application Archive. It is the deployment format used for Java web applications that run on servlet containers like Tomcat.

- Why it is used:
  Tomcat deploys WAR files directly.
- What it handles here:
  Bundles compiled classes, JSP views, CSS, configuration files, and dependency jars into one deployable file.
- Where it appears:
  `pom.xml` uses `<packaging>war</packaging>`
  Output goes to `target/travel-management-system.war`

## 4. Jakarta Servlet 6

Jakarta Servlet is the server-side web framework used to handle HTTP requests and responses.

- Why it is used:
  It is the controller layer of the application.
- What it handles here:
  URL mapping, request parameter reading, validation, forwarding to JSP pages, and form submission handling.
- Where it appears:
  `src/main/java/com/travel/servlet/`
- Example servlets in this project:
  `DashboardServlet`, `PackagesServlet`, `CreateBookingServlet`, `AddReviewServlet`, `AddUserServlet`

## 5. JSP

JSP stands for JavaServer Pages. It is the view technology used to generate HTML on the server side.

- Why it is used:
  It lets the backend pass dynamic data directly into page templates.
- What it handles here:
  Page layout, rendering package cards, showing dashboard values, listing payments, and rendering booking forms.
- Where it appears:
  `src/main/webapp/WEB-INF/views/`
- Important note:
  The JSP files are inside `WEB-INF`, so they cannot be accessed directly from the browser. They are rendered through servlets.

## 6. JSTL

JSTL stands for Jakarta Standard Tag Library. It adds reusable JSP tags for loops, conditions, and formatting.

- Why it is used:
  It keeps JSP pages cleaner than writing Java code directly inside them.
- What it handles here:
  `c:forEach`, `c:if`, `c:choose`, and number formatting with `fmt:formatNumber`.
- Where it appears:
  JSP files in `src/main/webapp/WEB-INF/views/`
- Dependency location:
  `pom.xml`

## 7. JDBC

JDBC stands for Java Database Connectivity. It is the standard Java API for talking to relational databases.

- Why it is used:
  It gives direct SQL-level access to MySQL.
- What it handles here:
  Opening database connections, running SQL queries, reading result sets, and writing inserts and updates.
- Where it appears:
  `src/main/java/com/travel/db/DBConnection.java`
  `src/main/java/com/travel/dao/TravelDao.java`
- Important note:
  The project uses plain JDBC, not Hibernate or Spring Data.

## 8. DAO Pattern

DAO stands for Data Access Object. It is not a library, but it is an important architectural pattern used in this project.

- Why it is used:
  It separates SQL code from servlet code.
- What it handles here:
  Fetching packages, dashboard stats, bookings, payments, reviews, users, and creating new records.
- Where it appears:
  `src/main/java/com/travel/dao/TravelDao.java`
- Benefit:
  Servlets stay focused on HTTP logic while the DAO handles database logic.

## 9. MySQL

MySQL is the relational database used by this application.

- Why it is used:
  It stores all core business data in structured tables.
- What it stores here:
  Users, destinations, hotels, transport, packages, bookings, payments, reviews, and media tables.
- Where it appears:
  Schema: `sql/travel.sql`
  Seed data: `sql/seed-data.sql`
  Config: `src/main/resources/db.properties`

## 10. MySQL Connector/J

MySQL Connector/J is the JDBC driver that allows Java code to talk to MySQL.

- Why it is used:
  JDBC needs a database-specific driver to connect to MySQL.
- What it handles here:
  Translates Java database calls into MySQL-compatible communication.
- Where it appears:
  `pom.xml`
- Current version in this project:
  `9.6.0`

## 11. Apache Tomcat

Apache Tomcat is the servlet container used to run this application locally.

- Why it is used:
  It executes servlets, serves JSP pages, and hosts the WAR file.
- What it handles here:
  URL routing at runtime, servlet execution, JSP compilation, static asset serving, and session management.
- Local runtime used in this setup:
  Tomcat `10.1.52`
- Local path:
  `.tools/apache-tomcat-10.1.52`

## 12. HTML and CSS

HTML and CSS define the structure and styling of the user interface.

- Why they are used:
  JSP eventually renders HTML, and CSS controls the look and layout.
- What they handle here:
  Top navigation, dashboard hero section, package cards, forms, tables, spacing, and colors.
- Where they appear:
  JSP markup in `src/main/webapp/WEB-INF/views/`
  Styles in `src/main/webapp/assets/css/style.css`

## 13. SQL Scripts

SQL scripts are used to create and populate the database.

- Why they are used:
  They make setup repeatable.
- What they handle here:
  Table creation, schema definition, and sample data insertion.
- Where they appear:
  `sql/travel.sql`
  `sql/seed-data.sql`

## 14. Placeholder Image Service

This project currently uses demo image URLs from Picsum for package, hotel, destination, and profile visuals.

- Why it is used:
  It quickly provides working image URLs without storing local image files.
- What it handles here:
  Demo package images, profile images, destination visuals, and hotel photos.
- Where it appears:
  Seed data inserted through `sql/seed-data.sql`
- Important note:
  This is useful for demo data, but for production you would normally store your own images or use cloud storage.

## 15. Configuration File

The database connection settings are stored in a properties file.

- Why it is used:
  It separates credentials and connection settings from Java code.
- What it handles here:
  MySQL URL, username, password, and JDBC driver class.
- Where it appears:
  `src/main/resources/db.properties`

## 16. How Everything Works Together

The project follows a simple layered structure:

- Step 1:
  The browser sends a request such as `/packages`.
- Step 2:
  Tomcat receives the request and sends it to the mapped servlet.
- Step 3:
  The servlet reads parameters and calls methods from `TravelDao`.
- Step 4:
  `TravelDao` uses JDBC and MySQL Connector/J to query MySQL.
- Step 5:
  The servlet stores the result in request attributes.
- Step 6:
  The request is forwarded to a JSP page.
- Step 7:
  JSP and JSTL render the final HTML and CSS-based UI for the browser.

## 17. Quick Stack Summary

- Language:
  Java 17
- Build tool:
  Maven
- Packaging:
  WAR
- Web framework:
  Jakarta Servlet 6
- View layer:
  JSP + JSTL
- Data access:
  JDBC + DAO pattern
- Database:
  MySQL
- Driver:
  MySQL Connector/J
- Runtime server:
  Apache Tomcat 10.1
- UI layer:
  HTML + CSS

## 18. Conclusion

This project is a classic Java web application built with a straightforward and understandable stack. It does not depend on a heavy framework like Spring Boot. Instead, it uses core Java web technologies directly, which makes it useful for learning servlet architecture, JSP rendering, JDBC-based database work, and traditional WAR deployment.
