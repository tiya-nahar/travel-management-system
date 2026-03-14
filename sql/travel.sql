CREATE DATABASE IF NOT EXISTS travel_management_system;
USE travel_management_system;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    phone VARCHAR(15),
    role VARCHAR(20),
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS destinations (
    destination_id INT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(100),
    country VARCHAR(100),
    description TEXT,
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS hotels (
    hotel_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150),
    city VARCHAR(100),
    rating DECIMAL(2,1),
    address TEXT,
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS transport (
    transport_id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50),
    provider VARCHAR(100),
    seat_capacity INT
);

CREATE TABLE IF NOT EXISTS packages (
    package_id INT PRIMARY KEY AUTO_INCREMENT,
    destination_id INT,
    title VARCHAR(150),
    description TEXT,
    price DECIMAL(10,2),
    duration_days INT,
    max_people INT,
    main_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    package_id INT,
    booking_date DATE,
    travel_date DATE,
    number_of_people INT,
    status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (package_id) REFERENCES packages(package_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT,
    amount DECIMAL(10,2),
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    payment_date TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE TABLE IF NOT EXISTS package_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    package_id INT,
    image_url VARCHAR(255),
    caption VARCHAR(200),
    FOREIGN KEY (package_id) REFERENCES packages(package_id)
);

CREATE TABLE IF NOT EXISTS package_details (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    package_id INT,
    hotel_id INT,
    transport_id INT,
    FOREIGN KEY (package_id) REFERENCES packages(package_id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id),
    FOREIGN KEY (transport_id) REFERENCES transport(transport_id)
);

CREATE TABLE IF NOT EXISTS destination_media (
    media_id INT PRIMARY KEY AUTO_INCREMENT,
    destination_id INT,
    media_type VARCHAR(20),
    media_url VARCHAR(255),
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    package_id INT,
    rating INT,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (package_id) REFERENCES packages(package_id)
);

CREATE TABLE IF NOT EXISTS admin_users (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    role VARCHAR(30) DEFAULT 'Admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id INT NOT NULL,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(60),
    entity_id INT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES admin_users(admin_id)
);

CREATE TABLE IF NOT EXISTS experiences (
    experience_id INT PRIMARY KEY AUTO_INCREMENT,
    destination_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    type VARCHAR(60),
    price DECIMAL(10,2),
    duration_hours INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);

CREATE TABLE IF NOT EXISTS memories (
    memory_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    destination_id INT,
    image_url VARCHAR(255),
    caption VARCHAR(200),
    status VARCHAR(20) DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);

CREATE TABLE IF NOT EXISTS budget_rules (
    rule_id INT PRIMARY KEY AUTO_INCREMENT,
    min_budget DECIMAL(10,2) NOT NULL,
    max_budget DECIMAL(10,2) NOT NULL,
    min_days INT NOT NULL,
    max_days INT NOT NULL,
    recommendation VARCHAR(150) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trip_tags (
    tag_id INT PRIMARY KEY AUTO_INCREMENT,
    package_id INT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    FOREIGN KEY (package_id) REFERENCES packages(package_id)
);

CREATE TABLE IF NOT EXISTS pricing_rules (
    rule_id INT PRIMARY KEY AUTO_INCREMENT,
    label VARCHAR(80) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    per_person DECIMAL(10,2) NOT NULL,
    duration_multiplier DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS destination_info (
    info_id INT PRIMARY KEY AUTO_INCREMENT,
    destination_id INT NOT NULL,
    best_season VARCHAR(80),
    climate VARCHAR(120),
    highlights VARCHAR(255),
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);
