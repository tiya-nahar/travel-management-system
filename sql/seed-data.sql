USE travel_management_system;

INSERT INTO users (user_id, name, email, password, phone, role, profile_image, created_at)
VALUES
    (1, 'Aarav Sharma', 'aarav.sharma@example.com', 'pass123', '9876543210', 'Customer', 'https://picsum.photos/seed/profile-aarav/300/300', '2026-03-01 10:00:00'),
    (2, 'Diya Patel', 'diya.patel@example.com', 'pass123', '9876543211', 'Customer', 'https://picsum.photos/seed/profile-diya/300/300', '2026-03-01 10:05:00'),
    (3, 'Rohan Mehta', 'rohan.mehta@example.com', 'pass123', '9876543212', 'Customer', 'https://picsum.photos/seed/profile-rohan/300/300', '2026-03-01 10:10:00'),
    (4, 'Meera Nair', 'meera.nair@example.com', 'pass123', '9876543213', 'Customer', 'https://picsum.photos/seed/profile-meera/300/300', '2026-03-01 10:15:00'),
    (5, 'Kabir Singh', 'kabir.singh@example.com', 'pass123', '9876543214', 'Customer', 'https://picsum.photos/seed/profile-kabir/300/300', '2026-03-01 10:20:00'),
    (6, 'Admin User', 'admin@aerotrail.com', 'admin_pass', '9876543215', 'Admin', 'https://picsum.photos/seed/profile-admin/300/300', '2026-03-01 10:30:00')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    email = VALUES(email),
    password = VALUES(password),
    phone = VALUES(phone),
    role = VALUES(role),
    profile_image = VALUES(profile_image),
    created_at = VALUES(created_at);

INSERT INTO destinations (destination_id, city, country, description, image_url)
VALUES
    (1, 'Goa', 'India', 'Beachfront sunsets, water sports, and relaxed coastal stays.', 'https://picsum.photos/seed/goa-destination/1200/800'),
    (2, 'Jaipur', 'India', 'Palaces, forts, markets, and a strong heritage travel vibe.', 'https://picsum.photos/seed/jaipur-destination/1200/800'),
    (3, 'Munnar', 'India', 'Tea gardens, cool weather, and scenic hill views in Kerala.', 'https://picsum.photos/seed/munnar-destination/1200/800'),
    (4, 'Manali', 'India', 'Mountain drives, snow points, and adventure-friendly stays.', 'https://picsum.photos/seed/manali-destination/1200/800'),
    (5, 'Srinagar', 'India', 'Lakeside scenery, mountain backdrops, and premium valley stays.', 'https://picsum.photos/seed/srinagar-destination/1200/800'),
    (6, 'Port Blair', 'India', 'Island hopping, turquoise water, and coastal sightseeing in Andaman.', 'https://picsum.photos/seed/andaman-destination/1200/800')
ON DUPLICATE KEY UPDATE
    city = VALUES(city),
    country = VALUES(country),
    description = VALUES(description),
    image_url = VALUES(image_url);

INSERT INTO hotels (hotel_id, name, city, rating, address, image_url)
VALUES
    (1, 'Coral Bay Resort', 'Goa', 4.5, 'Candolim Beach Road, Goa', 'https://picsum.photos/seed/goa-hotel/1000/700'),
    (2, 'Rajmahal Heritage Stay', 'Jaipur', 4.7, 'MI Road, Jaipur', 'https://picsum.photos/seed/jaipur-hotel/1000/700'),
    (3, 'Misty Lake Retreat', 'Munnar', 4.6, 'Tea Estate View Point, Munnar', 'https://picsum.photos/seed/munnar-hotel/1000/700'),
    (4, 'Snowcrest Lodge', 'Manali', 4.4, 'Mall Road Extension, Manali', 'https://picsum.photos/seed/manali-hotel/1000/700'),
    (5, 'Dal View Palace', 'Srinagar', 4.8, 'Boulevard Road, Srinagar', 'https://picsum.photos/seed/srinagar-hotel/1000/700'),
    (6, 'Blue Horizon Stay', 'Port Blair', 4.5, 'Marine Hill, Port Blair', 'https://picsum.photos/seed/andaman-hotel/1000/700')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    city = VALUES(city),
    rating = VALUES(rating),
    address = VALUES(address),
    image_url = VALUES(image_url);

INSERT INTO transport (transport_id, type, provider, seat_capacity)
VALUES
    (1, 'Flight', 'IndiGo', 180),
    (2, 'Volvo Coach', 'ZingBus', 42),
    (3, 'Train', 'Vande Bharat', 320),
    (4, 'Ferry', 'Makruzz', 220)
ON DUPLICATE KEY UPDATE
    type = VALUES(type),
    provider = VALUES(provider),
    seat_capacity = VALUES(seat_capacity);

INSERT INTO packages (package_id, destination_id, title, description, price, duration_days, max_people, main_image, created_at)
VALUES
    (1, 1, 'Goa Beach Escape', '4-day Goa trip with beach stay, local transfers, breakfast, and sunset cruise add-ons.', 18999.00, 4, 6, 'https://picsum.photos/seed/goa-package/1200/800', '2026-03-01 11:00:00'),
    (2, 2, 'Jaipur Royal Heritage Trail', '5-day Jaipur package covering Amber Fort, City Palace, local shopping, and curated hotel stay.', 24999.00, 5, 8, 'https://picsum.photos/seed/jaipur-package/1200/800', '2026-03-01 11:10:00'),
    (3, 3, 'Kerala Backwater Retreat', '5-day Munnar and Kerala retreat with scenic drives, plantation views, and premium stay.', 21999.00, 5, 6, 'https://picsum.photos/seed/munnar-package/1200/800', '2026-03-01 11:20:00'),
    (4, 4, 'Manali Snow Adventure', '6-day mountain trip with Solang Valley sightseeing, local cab, and adventure-ready itinerary.', 27999.00, 6, 6, 'https://picsum.photos/seed/manali-package/1200/800', '2026-03-01 11:30:00'),
    (5, 5, 'Kashmir Valley Escape', '6-day premium Srinagar stay with lake views, garden visits, and valley excursions.', 32999.00, 6, 6, 'https://picsum.photos/seed/srinagar-package/1200/800', '2026-03-01 11:40:00'),
    (6, 6, 'Andaman Island Getaway', '6-day island package with ferry transfers, coastal hotel stay, and beach activity options.', 38999.00, 6, 6, 'https://picsum.photos/seed/andaman-package/1200/800', '2026-03-01 11:50:00')
ON DUPLICATE KEY UPDATE
    destination_id = VALUES(destination_id),
    title = VALUES(title),
    description = VALUES(description),
    price = VALUES(price),
    duration_days = VALUES(duration_days),
    max_people = VALUES(max_people),
    main_image = VALUES(main_image),
    created_at = VALUES(created_at);

INSERT INTO package_details (detail_id, package_id, hotel_id, transport_id)
VALUES
    (1, 1, 1, 1),
    (2, 2, 2, 3),
    (3, 3, 3, 3),
    (4, 4, 4, 2),
    (5, 5, 5, 1),
    (6, 6, 6, 4)
ON DUPLICATE KEY UPDATE
    package_id = VALUES(package_id),
    hotel_id = VALUES(hotel_id),
    transport_id = VALUES(transport_id);

INSERT INTO package_images (image_id, package_id, image_url, caption)
VALUES
    (1, 1, 'https://picsum.photos/seed/goa-gallery-1/1200/800', 'Sunset beach view'),
    (2, 2, 'https://picsum.photos/seed/jaipur-gallery-1/1200/800', 'Heritage city highlights'),
    (3, 3, 'https://picsum.photos/seed/munnar-gallery-1/1200/800', 'Tea garden landscape'),
    (4, 4, 'https://picsum.photos/seed/manali-gallery-1/1200/800', 'Snow adventure trail'),
    (5, 5, 'https://picsum.photos/seed/srinagar-gallery-1/1200/800', 'Lake and mountain frame'),
    (6, 6, 'https://picsum.photos/seed/andaman-gallery-1/1200/800', 'Island beach scene')
ON DUPLICATE KEY UPDATE
    package_id = VALUES(package_id),
    image_url = VALUES(image_url),
    caption = VALUES(caption);

INSERT INTO destination_media (media_id, destination_id, media_type, media_url)
VALUES
    (1, 1, 'image', 'https://picsum.photos/seed/goa-media/1200/800'),
    (2, 2, 'image', 'https://picsum.photos/seed/jaipur-media/1200/800'),
    (3, 3, 'image', 'https://picsum.photos/seed/munnar-media/1200/800'),
    (4, 4, 'image', 'https://picsum.photos/seed/manali-media/1200/800'),
    (5, 5, 'image', 'https://picsum.photos/seed/srinagar-media/1200/800'),
    (6, 6, 'image', 'https://picsum.photos/seed/andaman-media/1200/800')
ON DUPLICATE KEY UPDATE
    destination_id = VALUES(destination_id),
    media_type = VALUES(media_type),
    media_url = VALUES(media_url);

INSERT INTO bookings (booking_id, user_id, package_id, booking_date, travel_date, number_of_people, status)
VALUES
    (1, 1, 1, '2026-03-02', '2026-04-12', 2, 'Confirmed'),
    (2, 2, 2, '2026-03-03', '2026-04-20', 3, 'Pending'),
    (3, 3, 4, '2026-03-04', '2026-05-08', 4, 'Confirmed'),
    (4, 4, 5, '2026-03-05', '2026-05-16', 2, 'Cancelled'),
    (5, 5, 6, '2026-03-06', '2026-06-02', 2, 'Confirmed')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    package_id = VALUES(package_id),
    booking_date = VALUES(booking_date),
    travel_date = VALUES(travel_date),
    number_of_people = VALUES(number_of_people),
    status = VALUES(status);

INSERT INTO payments (payment_id, booking_id, amount, payment_method, payment_status, payment_date)
VALUES
    (1, 1, 37998.00, 'UPI', 'Paid', '2026-03-02 12:15:00'),
    (2, 2, 74997.00, 'Card', 'Pending', '2026-03-03 14:30:00'),
    (3, 3, 111996.00, 'Net Banking', 'Paid', '2026-03-04 16:45:00'),
    (4, 4, 65998.00, 'Card', 'Pending', '2026-03-05 18:00:00'),
    (5, 5, 77998.00, 'UPI', 'Paid', '2026-03-06 09:25:00')
ON DUPLICATE KEY UPDATE
    booking_id = VALUES(booking_id),
    amount = VALUES(amount),
    payment_method = VALUES(payment_method),
    payment_status = VALUES(payment_status),
    payment_date = VALUES(payment_date);

INSERT INTO reviews (review_id, user_id, package_id, rating, comment, created_at)
VALUES
    (1, 1, 1, 5, 'Amazing beach property, smooth booking flow, and very good value for the price.', '2026-03-03 09:10:00'),
    (2, 2, 2, 4, 'Great heritage experience and comfortable stay. Shopping stops were a plus.', '2026-03-04 11:20:00'),
    (3, 3, 4, 5, 'Manali itinerary was packed and scenic. Hotel and transport were handled well.', '2026-03-05 17:45:00'),
    (4, 4, 3, 4, 'Peaceful hill views and nice resort atmosphere. Good choice for a short retreat.', '2026-03-06 13:05:00'),
    (5, 5, 6, 5, 'Island package felt premium and the beach visuals were exactly what we wanted.', '2026-03-07 19:30:00')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    package_id = VALUES(package_id),
    rating = VALUES(rating),
    comment = VALUES(comment),
    created_at = VALUES(created_at);
