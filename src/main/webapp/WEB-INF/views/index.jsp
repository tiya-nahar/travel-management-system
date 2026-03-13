<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Discover" />
<%@ include file="fragments/header.jspf" %>

<section class="hero">
    <div class="hero-copy">
        <p class="eyebrow">All in one travel platform</p>
        <h1>Build trips that match your vibe, budget, and time.</h1>
        <p class="lead">Search stays, flights, and holiday packages together. Then generate a ready-to-go itinerary in minutes.</p>
        <div class="chip-row">
            <span class="chip">24x7 support</span>
            <span class="chip">Verified stays</span>
            <span class="chip">Flexible plans</span>
        </div>
    </div>

    <div class="hero-card">
        <div class="tab-row">
            <button class="tab-btn active" type="button">Flights</button>
            <button class="tab-btn" type="button">Hotels</button>
            <button class="tab-btn" type="button">Trains</button>
            <button class="tab-btn" type="button">Cabs</button>
            <button class="tab-btn" type="button">Holidays</button>
        </div>
        <form class="search-card">
            <div class="search-grid">
                <div class="input-wrap">
                    <label for="fromCity">From</label>
                    <input id="fromCity" type="text" placeholder="Delhi" />
                </div>
                <div class="input-wrap">
                    <label for="toCity">To</label>
                    <input id="toCity" type="text" placeholder="Goa" />
                </div>
                <div class="input-wrap">
                    <label for="departDate">Depart</label>
                    <input id="departDate" type="date" />
                </div>
                <div class="input-wrap">
                    <label for="returnDate">Return</label>
                    <input id="returnDate" type="date" />
                </div>
                <div class="input-wrap">
                    <label for="travellers">Travellers</label>
                    <select id="travellers">
                        <option>1 Traveller</option>
                        <option>2 Travellers</option>
                        <option>3 Travellers</option>
                        <option>4+ Travellers</option>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="budget">Budget</label>
                    <select id="budget">
                        <option>INR 10k - 25k</option>
                        <option>INR 25k - 50k</option>
                        <option>INR 50k - 1L</option>
                        <option>INR 1L+</option>
                    </select>
                </div>
            </div>
            <div class="search-actions">
                <button class="btn btn-primary" type="button">Search Trips</button>
                <button class="btn btn-soft" type="button">Explore Deals</button>
            </div>
        </form>
    </div>
</section>

<section class="stat-strip">
    <div class="panel stat-card">
        <h3>Live Snapshot</h3>
        <p class="meta">Updated just now</p>
        <p>Total Users: <strong>${stats.users}</strong></p>
        <p>Total Packages: <strong>${stats.packages}</strong></p>
        <p>Active Bookings: <strong>${stats.bookings}</strong></p>
        <p>Revenue (Paid): <strong>Rs. <fmt:formatNumber value="${stats.revenue}" type="number" /></strong></p>
    </div>
    <div class="kpi-grid">
        <article class="panel kpi"><p>Total Destinations</p><strong>${stats.destinations}</strong></article>
        <article class="panel kpi"><p>Total Bookings</p><strong>${stats.bookings}</strong></article>
        <article class="panel kpi"><p>Pending Payments</p><strong>${stats.pendingPayments}</strong></article>
        <article class="panel kpi"><p>Total Reviews</p><strong>${stats.reviews}</strong></article>
    </div>
</section>

<section class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Top holiday packages</h2>
            <p>Curated itineraries from destinations, hotels, and transports.</p>
        </div>
    </div>
    <div class="package-grid">
        <c:forEach var="pkg" items="${topPackages}">
            <article class="card">
                <img src="${pkg.mainImage}" alt="${pkg.title}" />
                <div class="card-body">
                    <h3>${pkg.title}</h3>
                    <p class="meta">${pkg.city}, ${pkg.country} | ${pkg.durationDays} days</p>
                    <p class="meta">Hotel: ${pkg.hotelName} (${pkg.hotelRating} star)</p>
                    <p class="price">Rs. <fmt:formatNumber value="${pkg.price}" type="number" /></p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/packages">Book This</a>
                </div>
            </article>
        </c:forEach>
    </div>
</section>

<section class="section itinerary">
    <div class="itinerary-head">
        <div>
            <h2 class="section-title">Instant itinerary generator</h2>
            <p class="meta">Pick a city, pace, and interests. Get a ready plan with day wise suggestions.</p>
        </div>
        <button class="btn btn-ghost" type="button">Save &amp; Share</button>
    </div>
    <div class="itinerary-body">
        <form id="itineraryForm" class="panel itinerary-form">
            <div class="row">
                <div class="input-wrap">
                    <label for="tripCity">Destination</label>
                    <select id="tripCity">
                        <option value="Goa">Goa, India</option>
                        <option value="Manali">Manali, India</option>
                        <option value="Dubai">Dubai, UAE</option>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="tripDays">Days</label>
                    <select id="tripDays">
                        <option value="3">3 Days</option>
                        <option value="4">4 Days</option>
                        <option value="5">5 Days</option>
                        <option value="6">6 Days</option>
                    </select>
                </div>
            </div>
            <div class="row">
                <div class="input-wrap">
                    <label for="tripPace">Pace</label>
                    <select id="tripPace">
                        <option value="relaxed">Relaxed</option>
                        <option value="balanced">Balanced</option>
                        <option value="packed">Packed</option>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="tripBudget">Budget</label>
                    <select id="tripBudget">
                        <option value="value">Value</option>
                        <option value="comfort">Comfort</option>
                        <option value="premium">Premium</option>
                    </select>
                </div>
            </div>
            <div class="row full">
                <label class="input-wrap" style="margin-bottom: 6px;">Interests</label>
                <div class="check-row">
                    <label class="check-pill"><input type="checkbox" name="interest" value="culture" checked /> Culture</label>
                    <label class="check-pill"><input type="checkbox" name="interest" value="food" checked /> Food</label>
                    <label class="check-pill"><input type="checkbox" name="interest" value="nature" /> Nature</label>
                    <label class="check-pill"><input type="checkbox" name="interest" value="adventure" /> Adventure</label>
                    <label class="check-pill"><input type="checkbox" name="interest" value="shopping" /> Shopping</label>
                </div>
            </div>
            <button class="btn btn-primary" type="submit">Generate Itinerary</button>
        </form>
        <div id="itineraryOutput" class="panel itinerary-output">
            <div class="itinerary-summary">
                <h3>Your plan will appear here</h3>
                <p class="meta">Fill the form and hit generate to get a day wise plan.</p>
            </div>
            <div class="itinerary-days"></div>
        </div>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
