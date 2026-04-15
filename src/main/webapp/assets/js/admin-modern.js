(function () {
  function initPagination(table) {
    var pageSize = parseInt(table.getAttribute('data-page-size') || '5', 10);
    var rows = Array.from(table.querySelectorAll('tbody tr'));
    var paginationHost = table.closest('.card-body').querySelector('.pagination-wrap');

    if (!paginationHost || rows.length <= pageSize) {
      return;
    }

    var pageCount = Math.ceil(rows.length / pageSize);
    var currentPage = 1;

    function renderRows() {
      var start = (currentPage - 1) * pageSize;
      var end = start + pageSize;
      rows.forEach(function (row, idx) {
        row.style.display = idx >= start && idx < end ? '' : 'none';
      });
    }

    function renderPager() {
      var nav = document.createElement('nav');
      var ul = document.createElement('ul');
      ul.className = 'pagination pagination-sm mb-0';

      for (var i = 1; i <= pageCount; i++) {
        var li = document.createElement('li');
        li.className = 'page-item ' + (i === currentPage ? 'active' : '');
        var a = document.createElement('a');
        a.className = 'page-link';
        a.textContent = String(i);
        a.href = '#';
        a.setAttribute('data-page', String(i));
        li.appendChild(a);
        ul.appendChild(li);
      }

      nav.appendChild(ul);
      paginationHost.innerHTML = '';
      paginationHost.appendChild(nav);

      ul.addEventListener('click', function (event) {
        var target = event.target;
        if (!(target instanceof HTMLElement)) return;
        if (!target.classList.contains('page-link')) return;
        event.preventDefault();
        currentPage = parseInt(target.getAttribute('data-page') || '1', 10);
        renderRows();
        renderPager();
      });
    }

    renderRows();
    renderPager();
  }

  function initSidebarToggle() {
    var sidebar = document.getElementById('sidebarNav');
    var toggle = document.getElementById('toggleSidebar');
    if (!sidebar || !toggle) return;

    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('show');
    });
  }

  function initAlertsAndNotifications() {
    var host = document.getElementById('inlineAlertHost');
    var showAlertBtn = document.getElementById('showAlertBtn');
    var notifyBtn = document.getElementById('notifyBtn');
    var toastEl = document.getElementById('liveToast');
    var toastMsg = document.getElementById('toastMessage');
    var toast = toastEl ? new bootstrap.Toast(toastEl) : null;

    if (showAlertBtn && host) {
      showAlertBtn.addEventListener('click', function () {
        host.innerHTML = [
          '<div class="alert alert-warning alert-dismissible fade show" role="alert">',
          '<strong>Heads up!</strong> 12 bookings are pending manual verification.',
          '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
          '</div>'
        ].join('');
      });
    }

    if (notifyBtn && toast && toastMsg) {
      notifyBtn.addEventListener('click', function () {
        toastMsg.textContent = 'Notification: New payment received for Booking #B1007.';
        toast.show();
      });
    }
  }

  document.querySelectorAll('.paginated-table').forEach(initPagination);
  initSidebarToggle();
  initAlertsAndNotifications();
})();
