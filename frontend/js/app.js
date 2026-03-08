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

  function renderStats() {
    const paid = db.payments.filter((p) => p.payment_status === 'Paid').reduce((sum, p) => sum + p.amount, 0);
    if ($('#liveStats').length) {
      $('#liveStats').html(`
        <h3>Live Snapshot</h3>
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
      return `<article class='review-item'><p><strong>${user?.name || 'Guest'}</strong> on <strong>${pkg?.title || 'Package'}</strong></p><p>${'?'.repeat(r.rating)}${'?'.repeat(5 - r.rating)}</p><p>${r.comment}</p></article>`;
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

  function rerenderAll() {
    renderStats();
    renderPackages();
    renderPackages('#topPackages');
    renderBookingTable();
    renderPaymentTable();
    renderReviews();
    renderHub();
  }

  $('#searchInput, #cityFilter, #durationFilter, #sortFilter').on('input change', () => renderPackages());
  $(document).on('click', '.book-now', function () { openBookingModal(Number($(this).data('id'))); });
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

    rerenderAll();
    closeBookingModal();
    this.reset();
  });

  $('#reviewForm').on('submit', function (e) {
    e.preventDefault();
    db.reviews.push({ review_id: db.reviews.length ? db.reviews[db.reviews.length - 1].review_id + 1 : 1, user_id: Number($('#reviewUser').val()), package_id: Number($('#reviewPackage').val()), rating: Number($('#reviewRating').val()), comment: $('#reviewComment').val() });
    renderReviews();
    this.reset();
  });

  setActiveTopLinks();
  setFooterYear();
  fillSelects();
  renderPackageFilters();
  rerenderAll();
});
