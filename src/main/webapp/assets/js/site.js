(function () {
  function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);

    requestAnimationFrame(function () {
      toast.classList.add('show');
    });

    setTimeout(function () {
      toast.classList.remove('show');
      setTimeout(function () {
        toast.remove();
      }, 200);
    }, 2200);
  }

  function scrollToSection(id) {
    const target = document.getElementById(id);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  function buildItinerary(days, city, pace, interests, budget) {
    const morning = ['Cafe breakfast and local market', 'Sunrise viewpoint', 'Old town heritage walk', 'Street art trail'];
    const afternoon = ['Museum or cultural center', 'Local food trail', 'Waterfront stroll', 'Guided city tour'];
    const evening = ['Rooftop dinner', 'Night market and shopping', 'Live music spot', 'Sunset cruise'];

    const pick = function (list, index) {
      return list[(index + list.length) % list.length];
    };
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

  const form = document.getElementById('itineraryForm');
  const output = document.getElementById('itineraryOutput');
  if (form && output) {
    form.addEventListener('submit', function (event) {
      event.preventDefault();
      const days = Number(document.getElementById('tripDays').value) || 3;
      const city = document.getElementById('tripCity').value || 'Your destination';
      const pace = document.getElementById('tripPace').value || 'balanced';
      const budget = document.getElementById('tripBudget').value || 'comfort';
      const interests = Array.from(document.querySelectorAll("input[name='interest']:checked")).map(function (element) {
        return element.value;
      });
      output.innerHTML = buildItinerary(days, city, pace, interests, budget);
    });
  }

  const searchBtn = document.getElementById('searchTripsBtn');
  const exploreBtn = document.getElementById('exploreDealsBtn');
  const saveShareBtn = document.getElementById('saveShareBtn');
  const tabButtons = Array.from(document.querySelectorAll('.tab-btn[data-mode]'));
  const sectionMap = {
    flights: 'flights',
    hotels: 'hotels',
    trains: 'trains',
    cabs: 'cabs',
    buses: 'buses',
    packages: 'packages'
  };

  function setMode(mode) {
    tabButtons.forEach(function (button) {
      button.classList.toggle('active', button.dataset.mode === mode);
    });

    if (!searchBtn) {
      return;
    }

    searchBtn.dataset.mode = mode;
    searchBtn.textContent = mode === 'packages' ? 'Search Holidays' : 'Search ' + mode.charAt(0).toUpperCase() + mode.slice(1);
  }

  tabButtons.forEach(function (button) {
    button.addEventListener('click', function () {
      setMode(button.dataset.mode || 'flights');
    });
  });

  if (searchBtn) {
    searchBtn.addEventListener('click', function () {
      const mode = searchBtn.dataset.mode || 'flights';
      scrollToSection(sectionMap[mode] || 'packages');
      showToast((mode === 'packages' ? 'Holiday' : mode.charAt(0).toUpperCase() + mode.slice(1)) + ' options loaded.');
    });
  }

  if (exploreBtn) {
    exploreBtn.addEventListener('click', function () {
      scrollToSection('packages');
    });
  }

  if (saveShareBtn) {
    saveShareBtn.addEventListener('click', function () {
      const text = output ? output.textContent.trim() : '';
      if (!text || text.includes('Your plan will appear here')) {
        showToast('Generate an itinerary first.');
        return;
      }

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(function () {
          showToast('Itinerary copied to clipboard.');
        }).catch(function () {
          showToast('Unable to copy itinerary.');
        });
        return;
      }

      showToast('Copy to clipboard is not available in this browser.');
    });
  }

  document.querySelectorAll('[data-scroll-target]').forEach(function (button) {
    button.addEventListener('click', function () {
      scrollToSection(button.getAttribute('data-scroll-target'));
    });
  });

  document.querySelectorAll('[data-toast]').forEach(function (button) {
    button.addEventListener('click', function () {
      showToast(button.getAttribute('data-toast') || 'Done.');
    });
  });

  function initAuthMotion() {
    const root = document.querySelector('[data-auth-root]');
    if (!root) {
      return;
    }

    requestAnimationFrame(function () {
      root.classList.add('is-ready');
    });

    function setAuthMode(mode) {
      const isRegister = mode === 'register';
      root.classList.toggle('is-register', isRegister);
      root.classList.toggle('is-login', !isRegister);

      if (window.history && window.history.replaceState) {
        const url = new URL(window.location.href);
        if (isRegister) {
          url.searchParams.set('mode', 'register');
        } else {
          url.searchParams.delete('mode');
        }
        window.history.replaceState(null, '', url.toString());
      }
    }

    root.querySelectorAll('[data-auth-toggle]').forEach(function (button) {
      button.addEventListener('click', function () {
        setAuthMode(button.getAttribute('data-auth-toggle'));
      });
    });

    root.querySelectorAll('.auth-input, .auth-duo-input').forEach(function (input) {
      input.addEventListener('focus', function () {
        const card = input.closest('[data-auth-panel], [data-auth-card]');
        if (card) {
          card.classList.add('is-focused');
        }
      });

      input.addEventListener('blur', function () {
        const card = input.closest('[data-auth-panel], [data-auth-card]');
        if (!card) {
          return;
        }

        setTimeout(function () {
          if (!card.contains(document.activeElement)) {
            card.classList.remove('is-focused');
          }
        }, 0);
      });
    });
  }

  initAuthMotion();
})();
