(function () {
  const form = document.getElementById('itineraryForm');
  const output = document.getElementById('itineraryOutput');
  if (!form || !output) return;

  function buildItinerary(days, city, pace, interests, budget) {
    const morning = ['Cafe breakfast and local market', 'Sunrise viewpoint', 'Old town heritage walk', 'Street art trail'];
    const afternoon = ['Museum or cultural center', 'Local food trail', 'Waterfront stroll', 'Guided city tour'];
    const evening = ['Rooftop dinner', 'Night market and shopping', 'Live music spot', 'Sunset cruise'];

    const pick = (list, i) => list[(i + list.length) % list.length];
    const paceNote = pace === 'packed' ? 'Full day with back to back activities.' : pace === 'relaxed' ? 'Easy pace with long breaks.' : 'Balanced pace with room to explore.';
    const budgetNote = budget === 'premium' ? 'Premium stays and curated experiences.' : budget === 'value' ? 'Value stays and local picks.' : 'Comfort hotels with easy transfers.';

    let html = '<div class="itinerary-summary"><h3>' + city + ' - ' + days + ' day plan</h3>';
    html += '<p class="meta">' + paceNote + ' ' + budgetNote + '</p></div>';
    html += '<div class="itinerary-days">';

    for (let i = 1; i <= days; i += 1) {
      html += '<div class="itinerary-day">';
      html += '<strong>Day ' + i + '</strong>';
      html += '<p>Morning: ' + pick(morning, i) + '</p>';
      html += '<p>Afternoon: ' + pick(afternoon, i * 2) + '</p>';
      html += '<p>Evening: ' + pick(evening, i * 3) + '</p>';
      html += '<p class="meta">Focus: ' + (interests.length ? interests.join(', ') : 'Sightseeing') + '</p>';
      html += '</div>';
    }

    html += '</div>';
    return html;
  }

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    const days = Number(document.getElementById('tripDays').value) || 3;
    const city = document.getElementById('tripCity').value || 'Your destination';
    const pace = document.getElementById('tripPace').value || 'balanced';
    const budget = document.getElementById('tripBudget').value || 'comfort';
    const interests = Array.from(document.querySelectorAll("input[name='interest']:checked")).map((el) => el.value);
    output.innerHTML = buildItinerary(days, city, pace, interests, budget);
  });
})();
