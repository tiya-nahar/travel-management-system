<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Discover" />
<%@ include file="fragments/header.jspf" %>

<nav class="subnav" aria-label="Quick categories" style="justify-content: center;">
    <a class="subnav-link" href="#flights">Flights</a>
    <a class="subnav-link" href="#hotels">Hotels</a>
    <a class="subnav-link" href="#trains">Trains</a>
    <a class="subnav-link" href="#cabs">Cabs</a>
    <a class="subnav-link" href="#buses">Buses</a>
    <a class="subnav-link" href="#packages">Holidays</a>
    <a class="subnav-link" href="#itinerary">Itinerary</a>
</nav>

<section class="hero">
    <div class="hero-copy">
        <p class="eyebrow">All in one travel platform</p>
        <h1>Build trips that match your vibe, budget, and time.</h1>
        <p class="lead">Search stays, flights, trains, cabs, buses, and holidays together. Then generate a ready-to-go itinerary in minutes.</p>
        <div class="chip-row">
            <span class="chip">24x7 support</span>
            <span class="chip">Verified stays</span>
            <span class="chip">Flexible plans</span>
        </div>
    </div>

    <div class="hero-card">
        <div class="tab-row" style="justify-content: center;">
            <button class="tab-btn active" type="button" data-mode="flights">Flights</button>
            <button class="tab-btn" type="button" data-mode="hotels">Hotels</button>
            <button class="tab-btn" type="button" data-mode="trains">Trains</button>
            <button class="tab-btn" type="button" data-mode="cabs">Cabs</button>
            <button class="tab-btn" type="button" data-mode="buses">Buses</button>
            <button class="tab-btn" type="button" data-mode="packages">Holidays</button>
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
                <button id="searchTripsBtn" class="btn btn-primary" type="button" data-mode="flights">Search Flights</button>
                <button id="exploreDealsBtn" class="btn btn-soft" type="button">Explore Deals</button>
            </div>
        </form>
    </div>
</section>

<c:if test="${not empty message}">
    <div class="panel" style="margin-top: 16px;">${message}</div>
</c:if>

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

<section id="flights" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Flights made easy</h2>
            <p>Daily price drops, flexible reschedules, and quick boarding options.</p>
        </div>
        <button class="btn btn-soft" type="button" data-toast="Flight alerts enabled for your route.">Set Price Alert</button>
    </div>
    <div class="service-grid">
        <c:choose>
            <c:when test="${empty flightOptions}">
                <article class="service-card">
                    <h3>No flight providers yet</h3>
                    <p class="meta">Add flight transport rows to start showing live flight options.</p>
                </article>
            </c:when>
            <c:otherwise>
                <c:forEach var="flight" items="${flightOptions}">
                    <article class="service-card">
                        <h3>${flight.provider}</h3>
                        <p class="meta">Type: ${flight.type}</p>
                        <p class="meta">Seats per service: ${flight.seat_capacity}</p>
                        <button class="btn btn-primary" type="button" data-toast="${flight.provider} options loaded.">Check Fares</button>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="hotels" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Hotels &amp; stays</h2>
            <p>Curated stays for every budget with verified ratings.</p>
        </div>
        <button class="btn btn-soft" type="button" data-toast="Hotel recommendations refreshed.">Refresh Picks</button>
    </div>
    <div class="service-grid">
        <c:choose>
            <c:when test="${empty hotelOptions}">
                <article class="service-card">
                    <h3>No hotels available</h3>
                    <p class="meta">Seed hotel data to show stays here.</p>
                </article>
            </c:when>
            <c:otherwise>
                <c:forEach var="hotel" items="${hotelOptions}" varStatus="st">
                    <c:if test="${st.index lt 6}">
                        <article class="service-card">
                            <h3>${hotel.name}</h3>
                            <p class="meta">${hotel.city}</p>
                            <p class="meta">Rating: ${hotel.rating}</p>
                            <button class="btn btn-ghost" type="button" data-toast="${hotel.name} details opened.">View Stay</button>
                        </article>
                    </c:if>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="trains" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Trains &amp; rail passes</h2>
            <p>Fast bookings with seat availability and route alerts.</p>
        </div>
        <button class="btn btn-soft" type="button" data-toast="Rail pass options opened.">Rail Passes</button>
    </div>
    <div class="service-grid">
        <c:choose>
            <c:when test="${empty trainOptions}">
                <article class="service-card">
                    <h3>No train providers yet</h3>
                    <p class="meta">Add train transport data to enable this section.</p>
                </article>
            </c:when>
            <c:otherwise>
                <c:forEach var="train" items="${trainOptions}">
                    <article class="service-card">
                        <h3>${train.provider}</h3>
                        <p class="meta">Type: ${train.type}</p>
                        <p class="meta">Seat capacity: ${train.seat_capacity}</p>
                        <button class="btn btn-ghost" type="button" data-toast="${train.provider} routes loaded.">View Routes</button>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="cabs" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Cabs &amp; transfers</h2>
            <p>Airport pickups, local rides, and outstation transfers.</p>
        </div>
        <button class="btn btn-soft" type="button" data-toast="Cab quotes are ready.">Get Quote</button>
    </div>
    <div class="service-grid">
        <c:choose>
            <c:when test="${empty cabOptions}">
                <article class="service-card">
                    <h3>No cab providers yet</h3>
                    <p class="meta">Add cab transport rows to enable ride options.</p>
                </article>
            </c:when>
            <c:otherwise>
                <c:forEach var="cab" items="${cabOptions}">
                    <article class="service-card">
                        <h3>${cab.provider}</h3>
                        <p class="meta">Type: ${cab.type}</p>
                        <p class="meta">Seats: ${cab.seat_capacity}</p>
                        <button class="btn btn-primary" type="button" data-toast="${cab.provider} cabs are available.">Book Pickup</button>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="buses" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Buses &amp; road travel</h2>
            <p>Volvo, sleeper, and premium coaches with live seat map.</p>
        </div>
        <button class="btn btn-soft" type="button" data-toast="Bus routes loaded.">View Routes</button>
    </div>
    <div class="service-grid">
        <c:choose>
            <c:when test="${empty busOptions}">
                <article class="service-card">
                    <h3>No bus providers yet</h3>
                    <p class="meta">Add bus or coach transport entries to show routes.</p>
                </article>
            </c:when>
            <c:otherwise>
                <c:forEach var="bus" items="${busOptions}">
                    <article class="service-card">
                        <h3>${bus.provider}</h3>
                        <p class="meta">Type: ${bus.type}</p>
                        <p class="meta">Seats: ${bus.seat_capacity}</p>
                        <button class="btn btn-ghost" type="button" data-toast="${bus.provider} schedules loaded.">View Routes</button>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="packages" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Top holiday packages</h2>
            <p>Curated itineraries from destinations, hotels, and transports.</p>
        </div>
    </div>
    <div class="package-grid">
        <c:choose>
            <c:when test="${empty holidayPackages}">
                <div class="panel">No packages have been added yet.</div>
            </c:when>
            <c:otherwise>
                <c:forEach var="pkg" items="${holidayPackages}">
                    <article class="card">
                        <img src="${pkg.mainImage}" alt="${pkg.title}" />
                        <div class="card-body">
                            <h3>${pkg.title}</h3>
                            <p class="meta">${pkg.city}, ${pkg.country} | ${pkg.durationDays} days</p>
                            <p class="meta">Hotel: ${pkg.hotelName} (${pkg.hotelRating} star)</p>
                            <p class="price">Rs. <fmt:formatNumber value="${pkg.price}" type="number" /></p>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/packages#book">Book This</a>
                        </div>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="itinerary" class="section itinerary">
    <div class="itinerary-head">
        <div>
            <h2 class="section-title">Instant itinerary generator</h2>
            <p class="meta">Pick a city, pace, and interests. Get a ready plan with day wise suggestions.</p>
        </div>
        <button id="saveShareBtn" class="btn btn-ghost" type="button">Save &amp; Share</button>
    </div>
    <div class="itinerary-body">
        <form id="itineraryForm" class="panel itinerary-form">
            <div class="row">
                <div class="input-wrap">
                    <label for="tripCity">Destination</label>
                    <select id="tripCity">
                        <c:choose>
                            <c:when test="${empty cities}">
                                <option value="Your destination">No destinations available</option>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="city" items="${cities}">
                                    <option value="${city}">${city}</option>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
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

<section id="stories" class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">What people are saying</h2>
            <p>Live reviews coming directly from traveler accounts.</p>
        </div>
    </div>
    <div class="review-list">
        <c:choose>
            <c:when test="${empty latestReviews}">
                <div class="panel">No traveler reviews yet.</div>
            </c:when>
            <c:otherwise>
                <c:forEach var="review" items="${latestReviews}">
                    <article class="review-item">
                        <h3>${review.packageTitle}</h3>
                        <p class="meta">${review.comment}</p>
                        <strong>${review.userName}</strong>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
