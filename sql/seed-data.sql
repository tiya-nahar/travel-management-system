CREATE DATABASE IF NOT EXISTS travel_management_system;
USE travel_management_system;

DROP TEMPORARY TABLE IF EXISTS seed_destinations;
CREATE TEMPORARY TABLE seed_destinations (
    city VARCHAR(100) PRIMARY KEY,
    country VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(255)
);

INSERT INTO seed_destinations (city, country, description, image_url) VALUES
    ('Jaipur', 'India',
     'Historic forts, colorful bazaars, and royal heritage experiences.',
     'https://picsum.photos/seed/jaipur-palace/1200/800'),
    ('Udaipur', 'India',
     'Lakeside palaces, sunset boat rides, and relaxed old-city charm.',
     'https://picsum.photos/seed/udaipur-lake/1200/800'),
    ('Munnar', 'India',
     'Tea gardens, cool mountain weather, and scenic valley viewpoints.',
     'https://picsum.photos/seed/munnar-hills/1200/800'),
    ('Rishikesh', 'India',
     'River rafting, yoga retreats, and Himalayan foothill escapes.',
     'https://picsum.photos/seed/rishikesh-river/1200/800'),
    ('Darjeeling', 'India',
     'Tea estates, toy train routes, and panoramic mountain views.',
     'https://picsum.photos/seed/darjeeling-tea/1200/800'),
    ('Varanasi', 'India',
     'Ancient ghats, spiritual rituals, and immersive cultural walks.',
     'https://picsum.photos/seed/varanasi-ghat/1200/800');

UPDATE destinations d
JOIN seed_destinations s
    ON d.city = s.city
   AND d.country = s.country
SET d.description = s.description,
    d.image_url = s.image_url;

INSERT INTO destinations (city, country, description, image_url)
SELECT s.city, s.country, s.description, s.image_url
FROM seed_destinations s
LEFT JOIN destinations d
    ON d.city = s.city
   AND d.country = s.country
WHERE d.destination_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS seed_hotels;
CREATE TEMPORARY TABLE seed_hotels (
    name VARCHAR(150),
    city VARCHAR(100),
    rating DECIMAL(2,1),
    address TEXT,
    image_url VARCHAR(255),
    PRIMARY KEY (name, city)
);

INSERT INTO seed_hotels (name, city, rating, address, image_url) VALUES
    ('Amber Courtyard Retreat', 'Jaipur', 4.6,
     'Near Amer Fort Road, Jaipur, Rajasthan',
     'https://picsum.photos/seed/amber-courtyard/1200/800'),
    ('Lake Palace View Hotel', 'Udaipur', 4.7,
     'Pichola Lakefront, Udaipur, Rajasthan',
     'https://picsum.photos/seed/lake-palace-view/1200/800'),
    ('Tea Valley Residency', 'Munnar', 4.5,
     'Chithirapuram Hills, Munnar, Kerala',
     'https://picsum.photos/seed/tea-valley-residency/1200/800'),
    ('Ganga Riverside Stay', 'Rishikesh', 4.4,
     'Tapovan Riverside, Rishikesh, Uttarakhand',
     'https://picsum.photos/seed/ganga-riverside-stay/1200/800'),
    ('Summit Tea Estate Lodge', 'Darjeeling', 4.5,
     'Observatory Hill Road, Darjeeling, West Bengal',
     'https://picsum.photos/seed/summit-tea-estate/1200/800'),
    ('Ghat Heritage House', 'Varanasi', 4.3,
     'Dashashwamedh Ghat Road, Varanasi, Uttar Pradesh',
     'https://picsum.photos/seed/ghat-heritage-house/1200/800');

UPDATE hotels h
JOIN seed_hotels s
    ON h.name = s.name
   AND h.city = s.city
SET h.rating = s.rating,
    h.address = s.address,
    h.image_url = s.image_url;

INSERT INTO hotels (name, city, rating, address, image_url)
SELECT s.name, s.city, s.rating, s.address, s.image_url
FROM seed_hotels s
LEFT JOIN hotels h
    ON h.name = s.name
   AND h.city = s.city
WHERE h.hotel_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS seed_transport;
CREATE TEMPORARY TABLE seed_transport (
    type VARCHAR(50),
    provider VARCHAR(100) PRIMARY KEY,
    seat_capacity INT
);

INSERT INTO seed_transport (type, provider, seat_capacity) VALUES
    ('Flight', 'AeroTrail Air Connect', 180),
    ('Coach', 'AeroTrail RoadLink', 40),
    ('Cab', 'AeroTrail Private Transfer', 4);

UPDATE transport t
JOIN seed_transport s
    ON t.provider = s.provider
SET t.type = s.type,
    t.seat_capacity = s.seat_capacity;

INSERT INTO transport (type, provider, seat_capacity)
SELECT s.type, s.provider, s.seat_capacity
FROM seed_transport s
LEFT JOIN transport t
    ON t.provider = s.provider
WHERE t.transport_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS seed_packages;
CREATE TEMPORARY TABLE seed_packages (
    title VARCHAR(150) PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_days INT NOT NULL,
    max_people INT NOT NULL,
    main_image VARCHAR(255),
    hotel_name VARCHAR(150) NOT NULL,
    transport_provider VARCHAR(100) NOT NULL
);

INSERT INTO seed_packages
    (title, city, description, price, duration_days, max_people, main_image, hotel_name, transport_provider)
VALUES
    ('Jaipur Royal Weekend', 'Jaipur',
     'A short heritage break with fort visits, local cuisine, and market walks.',
     21999.00, 3, 6,
     'https://picsum.photos/seed/jaipur-royal-weekend/1200/800',
     'Amber Courtyard Retreat', 'AeroTrail RoadLink'),
    ('Udaipur Lake Escape', 'Udaipur',
     'A romantic lakeside stay with palace tours, old-city evenings, and boat rides.',
     26999.00, 4, 4,
     'https://picsum.photos/seed/udaipur-lake-escape/1200/800',
     'Lake Palace View Hotel', 'AeroTrail Private Transfer'),
    ('Munnar Tea Trail', 'Munnar',
     'Cool-weather hill holiday with tea gardens, waterfalls, and valley viewpoints.',
     23999.00, 4, 5,
     'https://picsum.photos/seed/munnar-tea-trail/1200/800',
     'Tea Valley Residency', 'AeroTrail Air Connect'),
    ('Rishikesh River Recharge', 'Rishikesh',
     'Adventure and wellness mix with rafting, cafe stops, and riverfront yoga.',
     19999.00, 3, 6,
     'https://picsum.photos/seed/rishikesh-river-recharge/1200/800',
     'Ganga Riverside Stay', 'AeroTrail RoadLink'),
    ('Darjeeling Mountain Calm', 'Darjeeling',
     'Tea estate views, sunrise points, and a relaxed Himalayan hill-station stay.',
     24999.00, 4, 4,
     'https://picsum.photos/seed/darjeeling-mountain-calm/1200/800',
     'Summit Tea Estate Lodge', 'AeroTrail Air Connect'),
    ('Varanasi Spiritual Circuit', 'Varanasi',
     'An immersive cultural trip with ghat walks, temple routes, and evening aarti.',
     18999.00, 3, 6,
     'https://picsum.photos/seed/varanasi-spiritual-circuit/1200/800',
     'Ghat Heritage House', 'AeroTrail Private Transfer');

UPDATE packages p
JOIN destinations d
    ON d.destination_id = p.destination_id
JOIN seed_packages s
    ON s.title = p.title
   AND s.city = d.city
SET p.description = s.description,
    p.price = s.price,
    p.duration_days = s.duration_days,
    p.max_people = s.max_people,
    p.main_image = s.main_image;

INSERT INTO packages (destination_id, title, description, price, duration_days, max_people, main_image)
SELECT d.destination_id, s.title, s.description, s.price, s.duration_days, s.max_people, s.main_image
FROM seed_packages s
JOIN destinations d
    ON d.city = s.city
LEFT JOIN packages p
    ON p.title = s.title
WHERE p.package_id IS NULL;

INSERT INTO package_details (package_id, hotel_id, transport_id)
SELECT p.package_id, h.hotel_id, t.transport_id
FROM seed_packages s
JOIN packages p
    ON p.title = s.title
JOIN hotels h
    ON h.name = s.hotel_name
   AND h.city = s.city
JOIN transport t
    ON t.provider = s.transport_provider
LEFT JOIN package_details pd
    ON pd.package_id = p.package_id
WHERE pd.detail_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS seed_destination_info;
CREATE TEMPORARY TABLE seed_destination_info (
    city VARCHAR(100) PRIMARY KEY,
    best_season VARCHAR(80),
    climate VARCHAR(120),
    highlights VARCHAR(255)
);

INSERT INTO seed_destination_info (city, best_season, climate, highlights) VALUES
    ('Jaipur', 'October to March', 'Warm days and cool evenings', 'Amer Fort, City Palace, Johari Bazaar'),
    ('Udaipur', 'September to March', 'Pleasant lakeside weather', 'Lake Pichola, City Palace, Sajjangarh'),
    ('Munnar', 'September to May', 'Cool and misty hills', 'Tea Gardens, Echo Point, Attukad Falls'),
    ('Rishikesh', 'September to April', 'Fresh mornings and sunny afternoons', 'Ganga Aarti, River Rafting, Laxman Jhula'),
    ('Darjeeling', 'October to April', 'Cool mountain climate', 'Tiger Hill, Tea Estates, Toy Train'),
    ('Varanasi', 'October to March', 'Mild winter and clear evenings', 'Dashashwamedh Ghat, Kashi Vishwanath, Ganga Aarti');

UPDATE destination_info i
JOIN destinations d
    ON d.destination_id = i.destination_id
JOIN seed_destination_info s
    ON s.city = d.city
SET i.best_season = s.best_season,
    i.climate = s.climate,
    i.highlights = s.highlights;

INSERT INTO destination_info (destination_id, best_season, climate, highlights)
SELECT d.destination_id, s.best_season, s.climate, s.highlights
FROM seed_destination_info s
JOIN destinations d
    ON d.city = s.city
LEFT JOIN destination_info i
    ON i.destination_id = d.destination_id
WHERE i.info_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS seed_destination_info;
DROP TEMPORARY TABLE IF EXISTS seed_packages;
DROP TEMPORARY TABLE IF EXISTS seed_transport;
DROP TEMPORARY TABLE IF EXISTS seed_hotels;
DROP TEMPORARY TABLE IF EXISTS seed_destinations;

-- Seed some demo users
INSERT INTO users (name, email, password, phone, role) VALUES
('John Doe', 'john@example.com', 'password123', '1234567890', 'Customer'),
('Jane Smith', 'jane@example.com', 'password123', '0987654321', 'Customer');

-- Backfill columns for existing databases that were created before booking detail fields.
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS traveler_name VARCHAR(80);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(15);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS special_request VARCHAR(250);

-- Seed demo bookings with richer traveler details from the updated booking form.
INSERT INTO bookings (
        user_id, package_id, booking_date, travel_date, number_of_people,
        traveler_name, contact_phone, special_request, status
)
SELECT u.user_id,
             p.package_id,
             CURDATE(),
             DATE_ADD(CURDATE(), INTERVAL 20 DAY),
             2,
             'John Doe',
             '9876543210',
             'Need an early check-in if possible.',
             'Pending'
FROM users u
JOIN packages p ON p.title = 'Jaipur Royal Weekend'
WHERE u.email = 'john@example.com'
    AND NOT EXISTS (
            SELECT 1
            FROM bookings b
            WHERE b.user_id = u.user_id
                AND b.package_id = p.package_id
                AND b.travel_date = DATE_ADD(CURDATE(), INTERVAL 20 DAY)
    );

INSERT INTO bookings (
        user_id, package_id, booking_date, travel_date, number_of_people,
        traveler_name, contact_phone, special_request, status
)
SELECT u.user_id,
             p.package_id,
             CURDATE(),
             DATE_ADD(CURDATE(), INTERVAL 35 DAY),
             4,
             'Jane Smith',
             '9123456780',
             'Looking for 2 adjacent rooms with mountain view.',
             'Confirmed'
FROM users u
JOIN packages p ON p.title = 'Darjeeling Mountain Calm'
WHERE u.email = 'jane@example.com'
    AND NOT EXISTS (
            SELECT 1
            FROM bookings b
            WHERE b.user_id = u.user_id
                AND b.package_id = p.package_id
                AND b.travel_date = DATE_ADD(CURDATE(), INTERVAL 35 DAY)
    );

INSERT INTO payments (booking_id, amount, payment_method, payment_status, payment_date)
SELECT b.booking_id,
             p.price * b.number_of_people,
             'UPI',
             CASE WHEN b.status = 'Confirmed' THEN 'Paid' ELSE 'Pending' END,
             NOW()
FROM bookings b
JOIN packages p ON p.package_id = b.package_id
LEFT JOIN payments pay ON pay.booking_id = b.booking_id
WHERE pay.payment_id IS NULL;

-- Create real admins and customers through the application or via SQL as needed.
