const db = {
  users: [
    { user_id: 1, name: 'Aarav Kapoor', email: 'aarav@mail.com', phone: '9876543210', role: 'Customer' },
    { user_id: 2, name: 'Meera Singh', email: 'meera@mail.com', phone: '9811122233', role: 'Customer' },
    { user_id: 3, name: 'Admin Team', email: 'admin@mail.com', phone: '9899999999', role: 'Admin' }
  ],
  destinations: [
    { destination_id: 1, city: 'Goa', country: 'India', description: 'Beaches and nightlife', image_url: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1200&q=80' },
    { destination_id: 2, city: 'Manali', country: 'India', description: 'Mountains and snow', image_url: 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1200&q=80' },
    { destination_id: 3, city: 'Dubai', country: 'UAE', description: 'Luxury city experience', image_url: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1200&q=80' }
  ],
  hotels: [
    { hotel_id: 1, name: 'Blue Palm Resort', city: 'Goa', rating: 4.5 },
    { hotel_id: 2, name: 'Himalayan Stay', city: 'Manali', rating: 4.7 },
    { hotel_id: 3, name: 'Skyline Marina', city: 'Dubai', rating: 4.8 }
  ],
  transport: [
    { transport_id: 1, type: 'Flight', provider: 'IndiGo', seat_capacity: 180 },
    { transport_id: 2, type: 'Volvo Bus', provider: 'HRTC', seat_capacity: 40 },
    { transport_id: 3, type: 'Flight', provider: 'Emirates', seat_capacity: 250 }
  ],
  packages: [
    { package_id: 1, destination_id: 1, title: 'Goa Beach Escape', description: '3N/4D beach retreat with water sports.', price: 18999, duration_days: 4, max_people: 6, main_image: 'https://images.unsplash.com/photo-1587922546307-776227941871?auto=format&fit=crop&w=1200&q=80' },
    { package_id: 2, destination_id: 2, title: 'Manali Adventure Loop', description: '5D mountain journey with valley tours.', price: 23999, duration_days: 5, max_people: 5, main_image: 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1200&q=80' },
    { package_id: 3, destination_id: 3, title: 'Dubai Premium Break', description: '4D city highlights and desert safari.', price: 49999, duration_days: 4, max_people: 4, main_image: 'https://images.unsplash.com/photo-1577717903315-1691ae25ab3f?auto=format&fit=crop&w=1200&q=80' }
  ],
  package_details: [
    { package_id: 1, hotel_id: 1, transport_id: 1 },
    { package_id: 2, hotel_id: 2, transport_id: 2 },
    { package_id: 3, hotel_id: 3, transport_id: 3 }
  ],
  bookings: [
    { booking_id: 1, user_id: 1, package_id: 1, booking_date: '2026-03-06', travel_date: '2026-04-04', number_of_people: 2, status: 'Confirmed' },
    { booking_id: 2, user_id: 2, package_id: 2, booking_date: '2026-03-07', travel_date: '2026-04-18', number_of_people: 3, status: 'Pending' }
  ],
  payments: [
    { payment_id: 1, booking_id: 1, amount: 37998, payment_method: 'UPI', payment_status: 'Paid', payment_date: '2026-03-06' },
    { payment_id: 2, booking_id: 2, amount: 71997, payment_method: 'Card', payment_status: 'Pending', payment_date: '2026-03-07' }
  ],
  reviews: [
    { review_id: 1, user_id: 1, package_id: 1, rating: 5, comment: 'Great stay and smooth booking.' },
    { review_id: 2, user_id: 2, package_id: 2, rating: 4, comment: 'Wonderful trip with good support.' }
  ]
};
