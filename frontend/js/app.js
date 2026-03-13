$(function () {
  function destinationById(id) { return db.destinations.find((d) => d.destination_id === id); }
  function packageDetail(pkgId) { return db.package_details.find((p) => p.package_id === pkgId); }
  function hotelById(id) { return db.hotels.find((h) => h.hotel_id === id); }

  function setActiveTopLinks() {
    const page = $('body').data('page');
    if (!page) return;
    $(`.top-links a[data-page='${page}']`).addClass('active');
  }

  function setFooterYear() {
    $('#year').text(new Date().getFullYear());
  }

  const storageKey = 'aerotrail_db';
  const stored = localStorage.getItem(storageKey);
  if (stored) {
    try { Object.assign(db, JSON.parse(stored)); } catch (e) { /* ignore */ }
  }

  function persist() {
    localStorage.setItem(storageKey, JSON.stringify(db));
  }

  function showToast(message) {
    const $toast = $(`<div class="toast">${message}</div>`);
    $('body').append($toast);
    setTimeout(() => $toast.addClass('show'), 10);
    setTimeout(() => {
      $toast.removeClass('show');
      setTimeout(() => $toast.remove(), 200);
    }, 2200);
  }

  function renderStats() {
    const paid = db.payments.filter((p) => p.payment_status === 'Paid').reduce((sum, p) => sum + p.amount, 0);
    if ($('#liveStats').length) {
      $('#liveStats').html(`
        <h3>Live Snapshot</h3>
        <p class="meta">Updated just now</p>
        <p>Total Users: <strong>${db.users.length}</strong></p>
        <p>Total Packages: <strong>${db.packages.length}</strong></p>
        <p>Active Bookings: <strong>${db.bookings.length}</strong></p>
        <p>Revenue (Paid): <strong>INR ${paid.toLocaleString('en-IN')}</strong></p>
      `);
    }
    if ($('#kpiGrid').length) {
      $('#kpiGrid').html(`
        <article class='panel kpi'><p>Total Destinations</p><strong>${db.destinations.length}</strong></article>
        <article class='panel kpi'><p>Total Bookings</p><strong>${db.bookings.length}</strong></article>
        <article class='panel kpi'><p>Pending Payments</p><strong>${db.payments.filter((p) => p.payment_status === 'Pending').length}</strong></article>
        <article class='panel kpi'><p>Total Reviews</p><strong>${db.reviews.length}</strong></article>
      `);
    }
  }

  function renderPackageFilters() {
    if (!$('#cityFilter').length) return;
    const cities = [...new Set(db.destinations.map((d) => d.city))];
    const $city = $('#cityFilter');
    $city.html('<option value="">All Cities</option>');
    cities.forEach((c) => $city.append(`<option value='${c}'>${c}</option>`));
  }

  function renderPackages(target = '#packageGrid') {
    if (!$(target).length) return;

    const query = ($('#searchInput').val() || '').toLowerCase().trim();
    const city = $('#cityFilter').val() || '';
    const maxDuration = parseInt($('#durationFilter').val(), 10);
    const sort = $('#sortFilter').val() || '';

    let list = db.packages.map((pkg) => {
      const destination = destinationById(pkg.destination_id);
      const detail = packageDetail(pkg.package_id);
      const hotel = detail ? hotelById(detail.hotel_id) : null;
      return { pkg, destination, hotel };
    });

    if ($('#searchInput').length || $('#cityFilter').length || $('#durationFilter').length) {
      list = list.filter(({ pkg, destination }) => {
        const matchesQuery = `${pkg.title} ${destination.city}`.toLowerCase().includes(query);
        const matchesCity = city ? destination.city === city : true;
        const matchesDuration = Number.isNaN(maxDuration) ? true : pkg.duration_days <= maxDuration;
        return matchesQuery && matchesCity && matchesDuration;
      });
    }

    if (sort === 'price_asc') list.sort((a, b) => a.pkg.price - b.pkg.price);
    if (sort === 'price_desc') list.sort((a, b) => b.pkg.price - a.pkg.price);
    if (sort === 'rating_desc') list.sort((a, b) => (b.hotel?.rating || 0) - (a.hotel?.rating || 0));

    if (!list.length) {
      $(target).html('<p class="panel">No packages matched filters.</p>');
      return;
    }

    $(target).html(list.map(({ pkg, destination, hotel }) => `
      <article class='card'>
        <img src='${pkg.main_image}' alt='${pkg.title}' />
        <div class='card-body'>
          <h3>${pkg.title}</h3>
          <p class='meta'>${destination.city}, ${destination.country} | ${pkg.duration_days} days</p>
          <p class='meta'>Hotel: ${hotel?.name || 'NA'} (${hotel?.rating || 0} star)</p>
          <p class='price'>INR ${pkg.price.toLocaleString('en-IN')}</p>
          <button class='btn btn-primary book-now' data-id='${pkg.package_id}'>Book This</button>
        </div>
      </article>
    `).join(''));
  }

  function renderDeals() {
    if (!$('#dealGrid').length) return;
    const deals = db.packages.slice().sort((a, b) => a.price - b.price).slice(0, 4);
    const html = deals.map((pkg) => {
      const destination = destinationById(pkg.destination_id);
      return `
        <article class="deal-card" data-city="${destination.city}">
          <div class="media" style="background-image: url('${pkg.main_image}');"></div>
          <div class="deal-body">
            <h3>${pkg.title}</h3>
            <p class="deal-meta">${destination.city}, ${destination.country} - ${pkg.duration_days} days</p>
            <p class="price">INR ${pkg.price.toLocaleString('en-IN')}</p>
            <button class="btn btn-primary book-now" data-id="${pkg.package_id}">Grab Deal</button>
          </div>
        </article>
      `;
    }).join('');

    $('#dealGrid').html(html);
  }

  function renderDestinations() {
    if (!$('#destinationGrid').length) return;
    const html = db.destinations.map((d) => `
      <article class="destination-card" data-city="${d.city}">
        <div class="media" style="background-image: url('${d.image_url}');"></div>
        <div class="dest-body">
          <h3>${d.city}</h3>
          <p class="meta">${d.country} - ${d.description}</p>
        </div>
      </article>
    `).join('');

    $('#destinationGrid').html(html);
  }

  function populateItineraryCities() {
    if (!$('#tripCity').length) return;
    const options = db.destinations.map((d) => `<option value="${d.city}">${d.city}, ${d.country}</option>`).join('');
    $('#tripCity').html(options || '<option value="Your destination">Your destination</option>');
  }

  function buildItinerary(days, city, pace, interests, budget) {
    const morning = ['Cafe breakfast and local market', 'Sunrise viewpoint', 'Old town heritage walk', 'Street art trail'];
    const afternoon = ['Museum or cultural center', 'Local food trail', 'Waterfront stroll', 'Guided city tour'];
    const evening = ['Rooftop dinner', 'Night market and shopping', 'Live music spot', 'Sunset cruise'];

    const pick = (list, i) => list[(i + list.length) % list.length];
    const paceNote = pace === 'packed' ? 'Full day with back to back activities.' : pace === 'relaxed' ? 'Easy pace with long breaks.' : 'Balanced pace with room to explore.';
    const budgetNote = budget === 'premium' ? 'Premium stays and curated experiences.' : budget === 'value' ? 'Value stays and local picks.' : 'Comfort hotels with easy transfers.';

    let output = `<div class="itinerary-summary"><h3>${city} - ${days} day plan</h3><p class="meta">${paceNote} ${budgetNote}</p></div><div class="itinerary-days">`;
    for (let i = 1; i <= days; i += 1) {
      output += `<div class="itinerary-day"><strong>Day ${i}</strong><p>Morning: ${pick(morning, i)}</p><p>Afternoon: ${pick(afternoon, i * 2)}</p><p>Evening: ${pick(evening, i * 3)}</p><p class="meta">Focus: ${interests.length ? interests.join(', ') : 'Sightseeing'}</p></div>`;
    }
    output += '</div>';
    return output;
  }

  function renderBookingTable() {
    if (!$('#bookingTableWrap').length) return;
    const rows = db.bookings.map((b) => {
      const user = db.users.find((u) => u.user_id === b.user_id);
      const pkg = db.packages.find((p) => p.package_id === b.package_id);
      return `<tr><td>${b.booking_id}</td><td>${user?.name || '-'}</td><td>${pkg?.title || '-'}</td><td>${b.booking_date}</td><td>${b.travel_date}</td><td>${b.number_of_people}</td><td><span class='badge ${b.status.toLowerCase()}'>${b.status}</span></td></tr>`;
    }).join('');

    $('#bookingTableWrap').html(`<table><thead><tr><th>ID</th><th>User</th><th>Package</th><th>Booking Date</th><th>Travel Date</th><th>People</th><th>Status</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderPaymentTable() {
    if (!$('#paymentTableWrap').length) return;
    const rows = db.payments.map((p) => {
      const statusClass = p.payment_status.toLowerCase() === 'paid' ? 'paid' : p.payment_status.toLowerCase();
      return `<tr><td>${p.payment_id}</td><td>${p.booking_id}</td><td>INR ${p.amount.toLocaleString('en-IN')}</td><td>${p.payment_method}</td><td><span class='badge ${statusClass}'>${p.payment_status}</span></td><td>${p.payment_date}</td></tr>`;
    }).join('');

    $('#paymentTableWrap').html(`<table><thead><tr><th>ID</th><th>Booking ID</th><th>Amount</th><th>Method</th><th>Status</th><th>Date</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderReviews() {
    if (!$('#reviewList').length) return;
    const html = db.reviews.slice().reverse().map((r) => {
      const user = db.users.find((u) => u.user_id === r.user_id);
      const pkg = db.packages.find((p) => p.package_id === r.package_id);
      return `<article class='review-item'><p><strong>${user?.name || 'Guest'}</strong> on <strong>${pkg?.title || 'Package'}</strong></p><p>${'*'.repeat(r.rating)}${'*'.repeat(5 - r.rating)}</p><p>${r.comment}</p></article>`;
    }).join('');

    $('#reviewList').html(html || '<p class="panel">No reviews yet.</p>');
  }

  function renderHub() {
    if ($('#userList').length) $('#userList').html(db.users.map((u) => `<li>${u.name} | ${u.role}</li>`).join(''));
    if ($('#destinationList').length) $('#destinationList').html(db.destinations.map((d) => `<li>${d.city}, ${d.country}</li>`).join(''));
    if ($('#hotelList').length) $('#hotelList').html(db.hotels.map((h) => `<li>${h.name} | ${h.city} (${h.rating})</li>`).join(''));
    if ($('#transportList').length) $('#transportList').html(db.transport.map((t) => `<li>${t.type} | ${t.provider} | ${t.seat_capacity}</li>`).join(''));
  }

  function fillSelects() {
    const userOptions = db.users.filter((u) => u.role === 'Customer').map((u) => `<option value='${u.user_id}'>${u.name}</option>`).join('');
    const packageOptions = db.packages.map((p) => `<option value='${p.package_id}'>${p.title}</option>`).join('');

    if ($('#bookingUser').length) $('#bookingUser').html(userOptions);
    if ($('#reviewUser').length) $('#reviewUser').html(userOptions);
    if ($('#bookingPackage').length) $('#bookingPackage').html(packageOptions);
    if ($('#reviewPackage').length) $('#reviewPackage').html(packageOptions);
  }

  function openBookingModal(pkgId) {
    if (!$('#bookingModal').length) {
      window.location.href = 'packages.html#book';
      return;
    }
    if (pkgId) $('#bookingPackage').val(pkgId);
    $('#bookingModal').removeClass('hidden');
  }

  function closeBookingModal() { $('#bookingModal').addClass('hidden'); }

  function applyPackageFiltersFromQuery() {
    if (!$('#packageGrid').length) return;
    const params = new URLSearchParams(window.location.search);
    const q = params.get('q');
    const city = params.get('city');
    if (q && $('#searchInput').length) $('#searchInput').val(q);
    if (city && $('#cityFilter').length) $('#cityFilter').val(city);
  }

  function rerenderAll() {
    renderStats();
    renderPackages();
    renderPackages('#topPackages');
    renderDeals();
    renderDestinations();
    renderBookingTable();
    renderPaymentTable();
    renderReviews();
    renderHub();
  }

  $('#searchInput, #cityFilter, #durationFilter, #sortFilter').on('input change', () => renderPackages());
  $(document).on('click', '.book-now', function () { openBookingModal(Number($(this).data('id'))); });
  $(document).on('click', '.deal-card, .destination-card', function (e) {
    if ($(e.target).closest('button, a').length) return;
    const city = $(this).data('city');
    if (city) window.location.href = `packages.html?city=${encodeURIComponent(city)}`;
  });
  $('#quickBookBtn, #openBookingBtn').on('click', () => openBookingModal());
  $('#closeBookingModal').on('click', closeBookingModal);
  $('#bookingModal').on('click', function (e) { if (e.target === this) closeBookingModal(); });

  $('#bookingForm').on('submit', function (e) {
    e.preventDefault();
    const packageId = Number($('#bookingPackage').val());
    const people = Number($('#peopleCount').val());
    const pkg = db.packages.find((p) => p.package_id === packageId);
    const bookingId = db.bookings.length ? db.bookings[db.bookings.length - 1].booking_id + 1 : 1;
    const paymentId = db.payments.length ? db.payments[db.payments.length - 1].payment_id + 1 : 1;
    const status = $('#bookingStatus').val();

    db.bookings.push({ booking_id: bookingId, user_id: Number($('#bookingUser').val()), package_id: packageId, booking_date: new Date().toISOString().slice(0, 10), travel_date: $('#travelDate').val(), number_of_people: people, status });
    db.payments.push({ payment_id: paymentId, booking_id: bookingId, amount: pkg.price * people, payment_method: $('#paymentMethod').val(), payment_status: status === 'Confirmed' ? 'Paid' : 'Pending', payment_date: new Date().toISOString().slice(0, 10) });

    persist();
    rerenderAll();
    closeBookingModal();
    this.reset();
    showToast('Booking created successfully.');
  });

  $('#reviewForm').on('submit', function (e) {
    e.preventDefault();
    db.reviews.push({ review_id: db.reviews.length ? db.reviews[db.reviews.length - 1].review_id + 1 : 1, user_id: Number($('#reviewUser').val()), package_id: Number($('#reviewPackage').val()), rating: Number($('#reviewRating').val()), comment: $('#reviewComment').val() });
    persist();
    renderReviews();
    this.reset();
    showToast('Review submitted.');
  });

  $('#itineraryForm').on('submit', function (e) {
    e.preventDefault();
    const days = Number($('#tripDays').val()) || 3;
    const city = $('#tripCity').val() || 'Your destination';
    const pace = $('#tripPace').val() || 'balanced';
    const budget = $('#tripBudget').val() || 'comfort';
    const interests = $("input[name='interest']:checked").map(function () { return $(this).val(); }).get();
    const html = buildItinerary(days, city, pace, interests, budget);
    $('#itineraryOutput').html(html);
  });

  $('#searchTripsBtn').on('click', function () {
    const mode = ($(this).data('mode') || 'flights').toString();
    const sectionMap = {
      flights: 'flights',
      hotels: 'hotels',
      trains: 'trains',
      cabs: 'cabs',
      buses: 'buses',
      holidays: 'packages'
    };
    const sectionId = sectionMap[mode] || 'packages';
    const target = document.getElementById(sectionId);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth' });
      showToast(`${mode.charAt(0).toUpperCase() + mode.slice(1)} options loaded.`);
      return;
    }
    const toCity = ($('#toCity').val() || '').trim();
    const q = toCity || ($('#fromCity').val() || '').trim();
    const url = q ? `packages.html?q=${encodeURIComponent(q)}&city=${encodeURIComponent(toCity)}` : 'packages.html';
    window.location.href = url;
  });

  $('#exploreDealsBtn, #viewAllOffersBtn').on('click', function () {
    const deals = document.getElementById('deals');
    if (deals) deals.scrollIntoView({ behavior: 'smooth' });
  });

  $(document).on('click', '[data-action="notify"]', function () {
    const msg = $(this).data('message') || 'Action completed.';
    showToast(msg);
  });

  $('.support-btn').on('click', function () {
    const modalId = 'supportModal';
    if (!document.getElementById(modalId)) {
      $('body').append(`
        <div id="${modalId}" class="modal hidden">
          <div class="modal-content">
            <button class="close-btn" data-close="support">x</button>
            <h3 style="margin-bottom: 8px;">Support</h3>
            <p class="meta" style="margin-bottom: 12px;">Tell us what you need help with and we will reach out.</p>
            <form id="supportForm" class="booking-form">
              <input id="supportName" type="text" placeholder="Your Name" required />
              <input id="supportEmail" type="email" placeholder="Email Address" required />
              <select id="supportTopic">
                <option value="booking">Booking Help</option>
                <option value="payment">Payment Issue</option>
                <option value="itinerary">Itinerary Assistance</option>
                <option value="other">Other</option>
              </select>
              <input id="supportMessage" type="text" placeholder="Brief message" required />
              <button class="btn btn-primary" type="submit">Submit Request</button>
            </form>
          </div>
        </div>
      `);
    }
    $(`#${modalId}`).removeClass('hidden');
  });

  $(document).on('click', '[data-close="support"]', function () {
    $('#supportModal').addClass('hidden');
  });

  $(document).on('click', '#supportModal', function (e) {
    if (e.target === this) $('#supportModal').addClass('hidden');
  });

  $(document).on('submit', '#supportForm', function (e) {
    e.preventDefault();
    showToast('Support request submitted.');
    $('#supportModal').addClass('hidden');
    this.reset();
  });

  $(document).on('click', '[data-info]', function (e) {
    e.preventDefault();
    const key = $(this).data('info');
    const infoMap = {
      'Help Center': 'Browse guides for bookings, cancellations, and trip changes.',
      'Safety': 'Partner verified stays and trusted transport providers.',
      'Refunds': 'Refunds are processed within 5-7 working days.',
      'About': 'AeroTrail helps you plan and book complete trips in one place.',
      'Careers': 'We are hiring travel designers, engineers, and support teams.',
      'Partner With Us': 'List your hotel or transport business with AeroTrail.'
    };
    const message = infoMap[key] || 'More details coming soon.';
    const modalId = 'infoModal';
    if (!document.getElementById(modalId)) {
      $('body').append(`
        <div id="${modalId}" class="modal hidden">
          <div class="modal-content">
            <button class="close-btn" data-close="info">x</button>
            <h3 id="infoTitle" style="margin-bottom: 8px;"></h3>
            <p id="infoMessage" class="meta"></p>
          </div>
        </div>
      `);
    }
    $('#infoTitle').text(key);
    $('#infoMessage').text(message);
    $(`#${modalId}`).removeClass('hidden');
  });

  $(document).on('click', '[data-close="info"]', function () {
    $('#infoModal').addClass('hidden');
  });

  $(document).on('click', '#infoModal', function (e) {
    if (e.target === this) $('#infoModal').addClass('hidden');
  });

  $('#saveShareBtn').on('click', function () {
    const text = $('#itineraryOutput').text().trim();
    if (!text || text.includes('Your plan will appear here')) {
      showToast('Generate an itinerary first.');
      return;
    }
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(() => showToast('Itinerary copied to clipboard.'));
    } else {
      window.prompt('Copy your itinerary:', text);
    }
  });

  $('.tab-btn').on('click', function () {
    $('.tab-btn').removeClass('active');
    $(this).addClass('active');
    const mode = $(this).text().trim();
    const modeKey = mode.toLowerCase();
    $('#searchTripsBtn').text(`Search ${mode}`);
    $('#searchTripsBtn').attr('data-mode', modeKey);
  });

  setActiveTopLinks();
  setFooterYear();
  fillSelects();
  renderPackageFilters();
  populateItineraryCities();
  applyPackageFiltersFromQuery();
  rerenderAll();
  if (window.location.hash === '#book') openBookingModal();
});
