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

  function ensureDefaults() {
    db.admin_users = db.admin_users || [];
    db.experiences = db.experiences || [];
    db.memories = db.memories || [];
    db.budget_rules = db.budget_rules || [];
    db.trip_tags = db.trip_tags || [];
    db.pricing_rules = db.pricing_rules || [];
    db.destination_info = db.destination_info || [];
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

  ensureDefaults();

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

  function nextId(list, key) {
    if (!list.length) return 1;
    return Math.max(...list.map((item) => Number(item[key]) || 0)) + 1;
  }

  function adminRequireAuth() {
    const page = $('body').data('page');
    if (page !== 'admin') return;
    if (localStorage.getItem('admin_auth') !== 'true') {
      window.location.href = 'admin-login.html';
    }
  }

  function renderAdminOverview() {
    if (!$('#adminStats').length) return;
    const stats = [
      { label: 'Total Users', value: db.users.length },
      { label: 'Total Bookings', value: db.bookings.length },
      { label: 'Packages', value: db.packages.length },
      { label: 'Memories', value: db.memories.length }
    ];
    $('#adminStats').html(stats.map((s) => `
      <article class="panel">
        <h3>${s.label}</h3>
        <p class="price">${s.value}</p>
      </article>
    `).join(''));

    const recent = db.bookings.slice().reverse().slice(0, 5).map((b) => {
      const user = db.users.find((u) => u.user_id === b.user_id);
      const pkg = db.packages.find((p) => p.package_id === b.package_id);
      return `<tr><td>${b.booking_id}</td><td>${user?.name || '-'}</td><td>${pkg?.title || '-'}</td><td>${b.travel_date}</td><td>${b.status}</td></tr>`;
    }).join('');
    $('#adminRecentBookings').html(`<table><thead><tr><th>ID</th><th>User</th><th>Package</th><th>Travel Date</th><th>Status</th></tr></thead><tbody>${recent || ''}</tbody></table>`);
  }

  function renderAdminPackages() {
    if (!$('#packageTable').length) return;
    const rows = db.packages.map((p) => {
      const d = destinationById(p.destination_id);
      return `<tr><td>${p.package_id}</td><td>${p.title}</td><td>${d?.city || '-'}</td><td>INR ${p.price.toLocaleString('en-IN')}</td><td><button class="btn btn-ghost" data-edit-package="${p.package_id}">Edit</button><button class="btn btn-soft" data-delete-package="${p.package_id}">Delete</button></td></tr>`;
    }).join('');
    $('#packageTable').html(`<table><thead><tr><th>ID</th><th>Title</th><th>City</th><th>Price</th><th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminDestinations() {
    if (!$('#destinationTable').length) return;
    const rows = db.destinations.map((d) => `<tr><td>${d.destination_id}</td><td>${d.city}</td><td>${d.country}</td><td><button class="btn btn-ghost" data-edit-destination="${d.destination_id}">Edit</button><button class="btn btn-soft" data-delete-destination="${d.destination_id}">Delete</button></td></tr>`).join('');
    $('#destinationTable').html(`<table><thead><tr><th>ID</th><th>City</th><th>Country</th><th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminExperiences() {
    if (!$('#experienceTable').length) return;
    const rows = db.experiences.map((e) => {
      const d = destinationById(e.destination_id);
      return `<tr><td>${e.experience_id}</td><td>${e.title}</td><td>${d?.city || '-'}</td><td>${e.type}</td><td>INR ${e.price}</td><td><button class="btn btn-ghost" data-edit-experience="${e.experience_id}">Edit</button><button class="btn btn-soft" data-delete-experience="${e.experience_id}">Delete</button></td></tr>`;
    }).join('');
    $('#experienceTable').html(`<table><thead><tr><th>ID</th><th>Title</th><th>Destination</th><th>Type</th><th>Price</th><th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminBookings() {
    if (!$('#bookingAdminTable').length) return;
    const rows = db.bookings.map((b) => {
      const user = db.users.find((u) => u.user_id === b.user_id);
      const pkg = db.packages.find((p) => p.package_id === b.package_id);
      return `<tr>
        <td>${b.booking_id}</td>
        <td>${user?.name || '-'}</td>
        <td>${pkg?.title || '-'}</td>
        <td>${b.travel_date}</td>
        <td>
          <select data-booking-status="${b.booking_id}">
            <option ${b.status === 'Confirmed' ? 'selected' : ''}>Confirmed</option>
            <option ${b.status === 'Pending' ? 'selected' : ''}>Pending</option>
            <option ${b.status === 'Cancelled' ? 'selected' : ''}>Cancelled</option>
          </select>
        </td>
        <td><button class="btn btn-primary" data-save-booking="${b.booking_id}">Save</button></td>
      </tr>`;
    }).join('');
    $('#bookingAdminTable').html(`<table><thead><tr><th>ID</th><th>User</th><th>Package</th><th>Travel Date</th><th>Status</th><th>Action</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminUsers() {
    if (!$('#userAdminTable').length) return;
    const rows = db.users.map((u) => {
      const status = u.status || 'Active';
      return `<tr><td>${u.user_id}</td><td>${u.name}</td><td>${u.email}</td><td>${u.role}</td><td>${status}</td><td><button class="btn btn-ghost" data-toggle-user="${u.user_id}">${status === 'Active' ? 'Disable' : 'Enable'}</button></td></tr>`;
    }).join('');
    $('#userAdminTable').html(`<table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminMemories() {
    if (!$('#memoryTable').length) return;
    const rows = db.memories.map((m) => {
      const user = db.users.find((u) => u.user_id === m.user_id);
      const dest = destinationById(m.destination_id);
      return `<tr><td>${m.memory_id}</td><td>${user?.name || '-'}</td><td>${dest?.city || '-'}</td><td>${m.caption}</td><td>${m.status}</td><td><button class="btn btn-primary" data-approve-memory="${m.memory_id}">Approve</button><button class="btn btn-soft" data-delete-memory="${m.memory_id}">Remove</button></td></tr>`;
    }).join('');
    $('#memoryTable').html(`<table><thead><tr><th>ID</th><th>User</th><th>Destination</th><th>Caption</th><th>Status</th><th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminBudgetRules() {
    if (!$('#budgetTable').length) return;
    const rows = db.budget_rules.map((r) => `<tr><td>${r.rule_id}</td><td>${r.min_budget}-${r.max_budget}</td><td>${r.min_days}-${r.max_days}</td><td>${r.recommendation}</td><td><button class="btn btn-ghost" data-edit-budget="${r.rule_id}">Edit</button><button class="btn btn-soft" data-delete-budget="${r.rule_id}">Delete</button></td></tr>`).join('');
    $('#budgetTable').html(`<table><thead><tr><th>ID</th><th>Budget</th><th>Days</th><th>Recommendation</th><th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminTripTags() {
    if (!$('#tripTagTable').length) return;
    const rows = db.trip_tags.map((t) => {
      const pkg = db.packages.find((p) => p.package_id === t.package_id);
      return `<tr><td>${pkg?.title || '-'}</td><td>${(t.tags || []).join(', ')}</td></tr>`;
    }).join('');
    $('#tripTagTable').html(`<table><thead><tr><th>Package</th><th>Tags</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminPricing() {
    if (!$('#pricingTable').length) return;
    const rows = db.pricing_rules.map((r) => `<tr><td>${r.rule_id}</td><td>${r.label}</td><td>${r.base_price}</td><td>${r.per_person}</td><td>${r.duration_multiplier}</td><td><button class="btn btn-ghost" data-edit-pricing="${r.rule_id}">Edit</button><button class="btn btn-soft" data-delete-pricing="${r.rule_id}">Delete</button></td></tr>`).join('');
    $('#pricingTable').html(`<table><thead><tr><th>ID</th><th>Label</th><th>Base</th><th>Per Person</th><th>Multiplier</th><th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminWeather() {
    if (!$('#weatherTable').length) return;
    const rows = db.destination_info.map((w) => {
      const d = destinationById(w.destination_id);
      return `<tr><td>${d?.city || '-'}</td><td>${w.best_season}</td><td>${w.climate}</td><td>${w.highlights}</td><td><button class="btn btn-ghost" data-edit-weather="${w.destination_id}">Edit</button></td></tr>`;
    }).join('');
    $('#weatherTable').html(`<table><thead><tr><th>Destination</th><th>Best Season</th><th>Climate</th><th>Highlights</th><th>Action</th></tr></thead><tbody>${rows}</tbody></table>`);
  }

  function renderAdminSelects() {
    if ($('#packageDestination').length) {
      $('#packageDestination').html(db.destinations.map((d) => `<option value="${d.destination_id}">${d.city}</option>`).join(''));
    }
    if ($('#experienceDestination').length) {
      $('#experienceDestination').html(db.destinations.map((d) => `<option value="${d.destination_id}">${d.city}</option>`).join(''));
    }
    if ($('#tripPackage').length) {
      $('#tripPackage').html(db.packages.map((p) => `<option value="${p.package_id}">${p.title}</option>`).join(''));
    }
    if ($('#weatherDestination').length) {
      $('#weatherDestination').html(db.destinations.map((d) => `<option value="${d.destination_id}">${d.city}</option>`).join(''));
    }
  }

  function renderAdminAll() {
    renderAdminOverview();
    renderAdminPackages();
    renderAdminDestinations();
    renderAdminExperiences();
    renderAdminBookings();
    renderAdminUsers();
    renderAdminMemories();
    renderAdminBudgetRules();
    renderAdminTripTags();
    renderAdminPricing();
    renderAdminWeather();
  }

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

  $('#adminLoginForm').on('submit', function (e) {
    e.preventDefault();
    const email = ($('#adminEmail').val() || '').trim().toLowerCase();
    const password = ($('#adminPassword').val() || '').trim();
    const admin = db.admin_users.find((a) => a.email.toLowerCase() === email && a.password === password);
    if (!admin) {
      showToast('Invalid admin credentials.');
      return;
    }
    localStorage.setItem('admin_auth', 'true');
    window.location.href = 'admin.html';
  });

  $('#adminLogoutBtn').on('click', function () {
    localStorage.removeItem('admin_auth');
    window.location.href = 'admin-login.html';
  });

  $('#adminRefreshBtn').on('click', function () {
    renderAdminAll();
    showToast('Dashboard refreshed.');
  });

  $('#packageForm').on('submit', function (e) {
    e.preventDefault();
    const id = Number($('#packageId').val());
    const payload = {
      package_id: id || nextId(db.packages, 'package_id'),
      destination_id: Number($('#packageDestination').val()),
      title: $('#packageTitle').val(),
      description: $('#packageDesc').val(),
      price: Number($('#packagePrice').val()),
      duration_days: Number($('#packageDuration').val()),
      max_people: 6,
      main_image: $('#packageImage').val()
    };
    const idx = db.packages.findIndex((p) => p.package_id === payload.package_id);
    if (idx >= 0) db.packages[idx] = payload; else db.packages.push(payload);
    $('#packageForm')[0].reset();
    $('#packageId').val('');
    persist();
    renderAdminAll();
    showToast('Package saved.');
  });

  $('#packageReset').on('click', function () {
    $('#packageForm')[0].reset();
    $('#packageId').val('');
  });

  $(document).on('click', '[data-edit-package]', function () {
    const id = Number($(this).data('edit-package'));
    const p = db.packages.find((x) => x.package_id === id);
    if (!p) return;
    $('#packageId').val(p.package_id);
    $('#packageTitle').val(p.title);
    $('#packageDestination').val(p.destination_id);
    $('#packageDuration').val(p.duration_days);
    $('#packagePrice').val(p.price);
    $('#packageImage').val(p.main_image);
    $('#packageDesc').val(p.description);
  });

  $(document).on('click', '[data-delete-package]', function () {
    const id = Number($(this).data('delete-package'));
    db.packages = db.packages.filter((p) => p.package_id !== id);
    persist();
    renderAdminAll();
  });

  $('#destinationForm').on('submit', function (e) {
    e.preventDefault();
    const id = Number($('#destinationId').val());
    const payload = {
      destination_id: id || nextId(db.destinations, 'destination_id'),
      city: $('#destinationCity').val(),
      country: $('#destinationCountry').val(),
      description: $('#destinationDesc').val(),
      image_url: $('#destinationImage').val()
    };
    const idx = db.destinations.findIndex((d) => d.destination_id === payload.destination_id);
    if (idx >= 0) db.destinations[idx] = payload; else db.destinations.push(payload);
    $('#destinationForm')[0].reset();
    $('#destinationId').val('');
    persist();
    renderAdminSelects();
    renderAdminAll();
    showToast('Destination saved.');
  });

  $('#destinationReset').on('click', function () {
    $('#destinationForm')[0].reset();
    $('#destinationId').val('');
  });

  $(document).on('click', '[data-edit-destination]', function () {
    const id = Number($(this).data('edit-destination'));
    const d = db.destinations.find((x) => x.destination_id === id);
    if (!d) return;
    $('#destinationId').val(d.destination_id);
    $('#destinationCity').val(d.city);
    $('#destinationCountry').val(d.country);
    $('#destinationImage').val(d.image_url);
    $('#destinationDesc').val(d.description);
  });

  $(document).on('click', '[data-delete-destination]', function () {
    const id = Number($(this).data('delete-destination'));
    db.destinations = db.destinations.filter((d) => d.destination_id !== id);
    persist();
    renderAdminSelects();
    renderAdminAll();
  });

  $('#experienceForm').on('submit', function (e) {
    e.preventDefault();
    const id = Number($('#experienceId').val());
    const payload = {
      experience_id: id || nextId(db.experiences, 'experience_id'),
      destination_id: Number($('#experienceDestination').val()),
      title: $('#experienceTitle').val(),
      type: $('#experienceType').val(),
      price: Number($('#experiencePrice').val() || 0),
      duration_hours: Number($('#experienceDuration').val() || 0)
    };
    const idx = db.experiences.findIndex((x) => x.experience_id === payload.experience_id);
    if (idx >= 0) db.experiences[idx] = payload; else db.experiences.push(payload);
    $('#experienceForm')[0].reset();
    $('#experienceId').val('');
    persist();
    renderAdminAll();
    showToast('Experience saved.');
  });

  $('#experienceReset').on('click', function () {
    $('#experienceForm')[0].reset();
    $('#experienceId').val('');
  });

  $(document).on('click', '[data-edit-experience]', function () {
    const id = Number($(this).data('edit-experience'));
    const x = db.experiences.find((e) => e.experience_id === id);
    if (!x) return;
    $('#experienceId').val(x.experience_id);
    $('#experienceTitle').val(x.title);
    $('#experienceDestination').val(x.destination_id);
    $('#experienceType').val(x.type);
    $('#experiencePrice').val(x.price);
    $('#experienceDuration').val(x.duration_hours);
  });

  $(document).on('click', '[data-delete-experience]', function () {
    const id = Number($(this).data('delete-experience'));
    db.experiences = db.experiences.filter((e) => e.experience_id !== id);
    persist();
    renderAdminAll();
  });

  $(document).on('click', '[data-save-booking]', function () {
    const id = Number($(this).data('save-booking'));
    const status = $(`[data-booking-status='${id}']`).val();
    const booking = db.bookings.find((b) => b.booking_id === id);
    if (booking) booking.status = status;
    persist();
    renderAdminAll();
    showToast('Booking updated.');
  });

  $(document).on('click', '[data-toggle-user]', function () {
    const id = Number($(this).data('toggle-user'));
    const user = db.users.find((u) => u.user_id === id);
    if (user) user.status = user.status === 'Disabled' ? 'Active' : 'Disabled';
    persist();
    renderAdminUsers();
  });

  $(document).on('click', '[data-approve-memory]', function () {
    const id = Number($(this).data('approve-memory'));
    const memory = db.memories.find((m) => m.memory_id === id);
    if (memory) memory.status = 'Approved';
    persist();
    renderAdminMemories();
  });

  $(document).on('click', '[data-delete-memory]', function () {
    const id = Number($(this).data('delete-memory'));
    db.memories = db.memories.filter((m) => m.memory_id !== id);
    persist();
    renderAdminMemories();
  });

  $('#budgetForm').on('submit', function (e) {
    e.preventDefault();
    const id = Number($('#budgetRuleId').val());
    const payload = {
      rule_id: id || nextId(db.budget_rules, 'rule_id'),
      min_budget: Number($('#budgetMin').val()),
      max_budget: Number($('#budgetMax').val()),
      min_days: Number($('#budgetDaysMin').val()),
      max_days: Number($('#budgetDaysMax').val()),
      recommendation: $('#budgetRecommendation').val()
    };
    const idx = db.budget_rules.findIndex((r) => r.rule_id === payload.rule_id);
    if (idx >= 0) db.budget_rules[idx] = payload; else db.budget_rules.push(payload);
    $('#budgetForm')[0].reset();
    $('#budgetRuleId').val('');
    persist();
    renderAdminBudgetRules();
  });

  $('#budgetReset').on('click', function () {
    $('#budgetForm')[0].reset();
    $('#budgetRuleId').val('');
  });

  $(document).on('click', '[data-edit-budget]', function () {
    const id = Number($(this).data('edit-budget'));
    const r = db.budget_rules.find((x) => x.rule_id === id);
    if (!r) return;
    $('#budgetRuleId').val(r.rule_id);
    $('#budgetMin').val(r.min_budget);
    $('#budgetMax').val(r.max_budget);
    $('#budgetDaysMin').val(r.min_days);
    $('#budgetDaysMax').val(r.max_days);
    $('#budgetRecommendation').val(r.recommendation);
  });

  $(document).on('click', '[data-delete-budget]', function () {
    const id = Number($(this).data('delete-budget'));
    db.budget_rules = db.budget_rules.filter((r) => r.rule_id !== id);
    persist();
    renderAdminBudgetRules();
  });

  $('#tripTagForm').on('submit', function (e) {
    e.preventDefault();
    const packageId = Number($('#tripPackage').val());
    const tags = ($('#tripTags').val() || '').split(',').map((t) => t.trim()).filter(Boolean);
    const existing = db.trip_tags.find((t) => t.package_id === packageId);
    if (existing) existing.tags = tags; else db.trip_tags.push({ package_id: packageId, tags });
    $('#tripTagForm')[0].reset();
    persist();
    renderAdminTripTags();
  });

  $('#pricingForm').on('submit', function (e) {
    e.preventDefault();
    const id = Number($('#pricingRuleId').val());
    const payload = {
      rule_id: id || nextId(db.pricing_rules, 'rule_id'),
      label: $('#pricingLabel').val(),
      base_price: Number($('#pricingBase').val()),
      per_person: Number($('#pricingPer').val()),
      duration_multiplier: Number($('#pricingMultiplier').val())
    };
    const idx = db.pricing_rules.findIndex((r) => r.rule_id === payload.rule_id);
    if (idx >= 0) db.pricing_rules[idx] = payload; else db.pricing_rules.push(payload);
    $('#pricingForm')[0].reset();
    $('#pricingRuleId').val('');
    persist();
    renderAdminPricing();
  });

  $('#pricingReset').on('click', function () {
    $('#pricingForm')[0].reset();
    $('#pricingRuleId').val('');
  });

  $(document).on('click', '[data-edit-pricing]', function () {
    const id = Number($(this).data('edit-pricing'));
    const r = db.pricing_rules.find((x) => x.rule_id === id);
    if (!r) return;
    $('#pricingRuleId').val(r.rule_id);
    $('#pricingLabel').val(r.label);
    $('#pricingBase').val(r.base_price);
    $('#pricingPer').val(r.per_person);
    $('#pricingMultiplier').val(r.duration_multiplier);
  });

  $(document).on('click', '[data-delete-pricing]', function () {
    const id = Number($(this).data('delete-pricing'));
    db.pricing_rules = db.pricing_rules.filter((r) => r.rule_id !== id);
    persist();
    renderAdminPricing();
  });

  $('#weatherForm').on('submit', function (e) {
    e.preventDefault();
    const destinationId = Number($('#weatherDestination').val());
    const payload = {
      destination_id: destinationId,
      best_season: $('#weatherSeason').val(),
      climate: $('#weatherClimate').val(),
      highlights: $('#weatherHighlights').val()
    };
    const idx = db.destination_info.findIndex((w) => w.destination_id === destinationId);
    if (idx >= 0) db.destination_info[idx] = payload; else db.destination_info.push(payload);
    $('#weatherForm')[0].reset();
    $('#weatherId').val('');
    persist();
    renderAdminWeather();
  });

  $('#weatherReset').on('click', function () {
    $('#weatherForm')[0].reset();
    $('#weatherId').val('');
  });

  $(document).on('click', '[data-edit-weather]', function () {
    const id = Number($(this).data('edit-weather'));
    const w = db.destination_info.find((x) => x.destination_id === id);
    if (!w) return;
    $('#weatherDestination').val(w.destination_id);
    $('#weatherSeason').val(w.best_season);
    $('#weatherClimate').val(w.climate);
    $('#weatherHighlights').val(w.highlights);
  });

  setActiveTopLinks();
  setFooterYear();
  fillSelects();
  renderPackageFilters();
  populateItineraryCities();
  applyPackageFiltersFromQuery();
  adminRequireAuth();
  renderAdminSelects();
  renderAdminAll();
  rerenderAll();
  if (window.location.hash === '#book') openBookingModal();
});
