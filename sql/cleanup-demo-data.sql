USE travel_management_system;

START TRANSACTION;

DELETE FROM admin_logs;
DELETE FROM payments;
DELETE FROM reviews;
DELETE FROM bookings;
DELETE FROM package_details;
DELETE FROM package_images;
DELETE FROM trip_tags;
DELETE FROM memories;
DELETE FROM experiences;
DELETE FROM destination_info;
DELETE FROM destination_media;
DELETE FROM budget_rules;
DELETE FROM pricing_rules;
DELETE FROM packages;
DELETE FROM hotels;
DELETE FROM transport;
DELETE FROM destinations;

DELETE FROM users
WHERE email LIKE '%@example.com'
   OR email IN ('admin@aerotrail.com', 'ops@aerotrail.com');

DELETE FROM admin_users
WHERE email LIKE '%@example.com'
   OR email IN ('admin@aerotrail.com', 'ops@aerotrail.com');

COMMIT;
