CREATE DATABASE travel_management_system;
USE travel_management_system;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    phone VARCHAR(15),
    role VARCHAR(20),
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE destinations (
    destination_id INT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(100),
    country VARCHAR(100),
    description TEXT,
    image_url VARCHAR(255)
);

CREATE TABLE hotels (
    hotel_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150),
    city VARCHAR(100),
    rating DECIMAL(2,1),
    address TEXT,
    image_url VARCHAR(255)
);

CREATE TABLE transport (
    transport_id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50),
    provider VARCHAR(100),
    seat_capacity INT
);

CREATE TABLE packages (
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

CREATE TABLE bookings (
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

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT,
    amount DECIMAL(10,2),
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    payment_date TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE TABLE package_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    package_id INT,
    image_url VARCHAR(255),
    caption VARCHAR(200),
    FOREIGN KEY (package_id) REFERENCES packages(package_id)
);

CREATE TABLE package_details (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    package_id INT,
    hotel_id INT,
    transport_id INT,
    FOREIGN KEY (package_id) REFERENCES packages(package_id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id),
    FOREIGN KEY (transport_id) REFERENCES transport(transport_id)
);

CREATE TABLE destination_media (
    media_id INT PRIMARY KEY AUTO_INCREMENT,
    destination_id INT,
    media_type VARCHAR(20),
    media_url VARCHAR(255),
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);

CREATE TABLE reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    package_id INT,
    rating INT,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (package_id) REFERENCES packages(package_id)
);