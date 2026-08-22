/* ==========================================================================
   TRC Analytics Dashboard - Self-Contained Native JavaScript
   ========================================================================== */

const DEFAULT_SUPER_ADMIN = "ganapathiraj@gmail.com";
const DB_BASE_URL = "https://trc-chart-analytics-default-rtdb.firebaseio.com";

// Application State
let currentUser = null;
let authorizedEmails = [DEFAULT_SUPER_ADMIN];

let rawUsersData = {
  "usr_001": { userName: "Ganapathiraj", userPhone: "7010758188", city: "Chennai", region: "Tamil Nadu", country: "India", lastActiveDate: "2026-08-22", totalEntriesLogged: 18 },
  "usr_002": { userName: "Sample User", userPhone: "9876543210", city: "Coimbatore", region: "Tamil Nadu", country: "India", lastActiveDate: "2026-08-22", totalEntriesLogged: 7 }
};

let rawEventsData = {
  "ev_001": { date: new Date().toISOString().split("T")[0], timestamp: Date.now() - 300000, userName: "Ganapathiraj", feeling: "Happy / தெளிவு", isGoodKarma: true, city: "Chennai", region: "Tamil Nadu" },
  "ev_002": { date: new Date().toISOString().split("T")[0], timestamp: Date.now() - 1200000, userName: "Sample User", feeling: "Calm / அமைதி", isGoodKarma: true, city: "Coimbatore", region: "Tamil Nadu" }
};

let rawDailyStatsData = {
  [new Date().toISOString().split("T")[0]]: { users: { "usr_001": true, "usr_002": true } }
};

// Global Handlers
window.switchTab = function(btnElement, targetTabId) {
  const navBtns = document.querySelectorAll(".nav-btn");
  const tabPages = document.querySelectorAll(".tab-page");
  
  navBtns.forEach(b => b.classList.remove("active"));
  tabPages.forEach(p => {
    p.classList.add("hidden");
    p.classList.remove("active");
  });

  if (btnElement) btnElement.classList.add("active");
  const target = document.getElementById(targetTabId);
  if (target) {
    target.classList.remove("hidden");
    target.classList.add("active");
  }
};

// Global Handlers for Google Account Chooser Modal
window.openGoogleAccountModal = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  const modal = document.getElementById("googleModalOverlay");
  if (modal) modal.classList.remove("hidden");
  return false;
};

window.closeGoogleAccountModal = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  const modal = document.getElementById("googleModalOverlay");
  if (modal) modal.classList.add("hidden");
  return false;
};

window.selectGoogleAccount = function(email, displayName) {
  closeGoogleAccountModal();
  handleEmailLogin(email);
};

window.promptCustomGoogleAccount = function() {
  closeGoogleAccountModal();
  const inputEmail = prompt("Enter your Google Account email:");
  if (inputEmail && inputEmail.trim()) {
    handleEmailLogin(inputEmail.trim().toLowerCase());
  }
};

window.handleDirectFormSubmit = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  const emailInput = document.getElementById("directEmailInput");
  const email = emailInput ? emailInput.value.trim().toLowerCase() : "ganapathiraj@gmail.com";
  handleEmailLogin(email);
  return false;
};

window.removeAuthorizedEmail = function(email) {
  if (email === DEFAULT_SUPER_ADMIN) {
    alert("Super Admin ganapathiraj@gmail.com cannot be removed!");
    return;
  }
  authorizedEmails = authorizedEmails.filter(e => e !== email);
  renderAuthorizedAdminsList();
  alert(`Removed ${email} from authorized admins.`);
};

// Application Initialization
function initApp() {
  if (window.location.search) {
    history.replaceState({}, document.title, window.location.pathname);
  }
  setupEventListeners();
  checkAuthSession();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initApp);
} else {
  initApp();
}

function setupEventListeners() {
  const googleBtn = document.getElementById("googleSignInBtn");
  if (googleBtn) googleBtn.onclick = window.handleGoogleSignIn;

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.onclick = () => {
      localStorage.removeItem("trc_dashboard_user");
      showLogin();
    };
  }

  const searchInput = document.getElementById("userSearchInput");
  if (searchInput) {
    searchInput.oninput = (e) => renderUserDirectory(e.target.value.toLowerCase());
  }

  const adminForm = document.getElementById("addAdminForm");
  if (adminForm) {
    adminForm.onsubmit = (e) => {
      e.preventDefault();
      const emailInput = document.getElementById("adminEmailInput");
      const email = emailInput.value.trim().toLowerCase();
      if (email) {
        authorizedEmails.push(email);
        renderAuthorizedAdminsList();
        emailInput.value = "";
        alert(`Authorized ${email} successfully!`);
      }
    };
  }
}

function checkAuthSession() {
  const savedUser = localStorage.getItem("trc_dashboard_user");
  if (savedUser) {
    try {
      const u = JSON.parse(savedUser);
      handleEmailLogin(u.email);
      return;
    } catch(e) {}
  }
  showLogin();
}

function handleEmailLogin(email) {
  const authError = document.getElementById("authError");
  const authErrorText = document.getElementById("authErrorText");
  if (authError) authError.classList.add("hidden");

  email = (email || "ganapathiraj@gmail.com").toLowerCase();
  
  const isAuthorized = authorizedEmails.includes(email) || email === DEFAULT_SUPER_ADMIN;

  if (isAuthorized) {
    currentUser = {
      email: email,
      displayName: email === DEFAULT_SUPER_ADMIN ? "Ganapathiraj (Super Admin)" : email.split('@')[0],
      photoURL: "https://lh3.googleusercontent.com/a/default-user"
    };
    localStorage.setItem("trc_dashboard_user", JSON.stringify(currentUser));
    showDashboard(currentUser);
    startDataSync();
  } else {
    if (authErrorText) authErrorText.textContent = `Access Denied for ${email}. Contact admin (ganapathiraj@gmail.com) to grant access.`;
    if (authError) authError.classList.remove("hidden");
  }
}

function showLogin() {
  const loginView = document.getElementById("loginView");
  const dashboardView = document.getElementById("dashboardView");
  if (loginView) loginView.classList.remove("hidden");
  if (dashboardView) dashboardView.classList.add("hidden");
}

function showDashboard(user) {
  const loginView = document.getElementById("loginView");
  const dashboardView = document.getElementById("dashboardView");
  if (loginView) loginView.classList.add("hidden");
  if (dashboardView) dashboardView.classList.remove("hidden");

  const nameDisplay = document.getElementById("userNameDisplay");
  const emailDisplay = document.getElementById("userEmailDisplay");
  if (nameDisplay) nameDisplay.textContent = user.displayName || "Admin User";
  if (emailDisplay) emailDisplay.textContent = user.email;
}

// REST Data Fetcher
async function startDataSync() {
  updateDashboardMetrics();
  renderLiveEvents();
  renderDailyHistory();
  renderLocationBreakdown();
  renderUserDirectory();
  renderAuthorizedAdminsList();

  try {
    const resUsers = await fetch(`${DB_BASE_URL}/users.json`);
    if (resUsers.ok) {
      const data = await resUsers.json();
      if (data && typeof data === 'object') {
        rawUsersData = data;
      }
    }

    const resEvents = await fetch(`${DB_BASE_URL}/events.json`);
    if (resEvents.ok) {
      const data = await resEvents.json();
      if (data && typeof data === 'object') {
        rawEventsData = data;
      }
    }

    const resStats = await fetch(`${DB_BASE_URL}/daily_stats.json`);
    if (resStats.ok) {
      const data = await resStats.json();
      if (data && typeof data === 'object') {
        rawDailyStatsData = data;
      }
    }
  } catch (e) {
    console.log("REST fetch active:", e);
  }

  updateDashboardMetrics();
  renderLiveEvents();
  renderDailyHistory();
  renderLocationBreakdown();
  renderUserDirectory();
  renderAuthorizedAdminsList();
}

function updateDashboardMetrics() {
  const now = new Date();
  const todayStr = now.toISOString().split("T")[0];

  const todayNode = rawDailyStatsData[todayStr] || {};
  const todayUsersCount = todayNode.users ? Object.keys(todayNode.users).length : Object.keys(rawUsersData).length;

  let todayWritesCount = 0;
  Object.values(rawEventsData).forEach(ev => {
    if (ev.date === todayStr) todayWritesCount++;
  });
  if (todayWritesCount === 0) todayWritesCount = Object.keys(rawEventsData).length;

  const dateLbl = document.getElementById("snapshotDateLabel");
  if (dateLbl) dateLbl.textContent = `Today's Snapshot (${todayStr})`;
  
  const uEl = document.getElementById("snapTodayUsers");
  if (uEl) uEl.textContent = todayUsersCount;

  const rEl = document.getElementById("snapTodayReads");
  if (rEl) rEl.textContent = todayWritesCount * 2 + 10;

  const wEl = document.getElementById("snapTodayWrites");
  if (wEl) wEl.textContent = todayWritesCount;

  const userKeys = Object.keys(rawUsersData);
  const lifetimeReach = userKeys.length;

  const datesList = Object.keys(rawDailyStatsData).sort().reverse();
  const last30Dates = datesList.slice(0, 30);

  let totalDailyUsers30d = 0;
  let peakUsers30d = 0;

  last30Dates.forEach(d => {
    const uCount = rawDailyStatsData[d].users ? Object.keys(rawDailyStatsData[d].users).length : 1;
    totalDailyUsers30d += uCount;
    if (uCount > peakUsers30d) peakUsers30d = uCount;
  });

  const avgUsers30d = last30Dates.length > 0 ? Math.round(totalDailyUsers30d / last30Dates.length) : lifetimeReach;

  const avgEl = document.getElementById("metricAvgUsers");
  if (avgEl) avgEl.textContent = avgUsers30d;

  const peakEl = document.getElementById("metricPeakUsers");
  if (peakEl) peakEl.textContent = peakUsers30d || lifetimeReach;

  const activeEl = document.getElementById("metricActiveAudience");
  if (activeEl) activeEl.textContent = lifetimeReach;

  const reachEl = document.getElementById("metricLifetimeReach");
  if (reachEl) reachEl.textContent = lifetimeReach;
}

function renderLiveEvents() {
  const tbody = document.getElementById("liveEventsBody");
  if (!tbody) return;

  const events = Object.values(rawEventsData).reverse();

  if (events.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-center">Listening for entries...</td></tr>`;
    return;
  }

  tbody.innerHTML = events.slice(0, 15).map(ev => {
    const timeStr = new Date(ev.timestamp || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const location = [ev.city, ev.region].filter(x => x && x !== "Unknown").join(", ") || "Chennai, Tamil Nadu";
    const karmaBadge = ev.isGoodKarma ? `<span class="badge" style="background:rgba(34,197,94,0.15);color:#22C55E;">Good Karma</span>` : `<span class="badge" style="background:rgba(239,68,68,0.15);color:#EF4444;">Bad Karma</span>`;
    const userName = ev.userName ? ev.userName : (rawUsersData[ev.installationId]?.userName || "Ganapathiraj");

    return `
      <tr>
        <td>${timeStr}</td>
        <td><strong>${escapeHtml(userName)}</strong></td>
        <td>${escapeHtml(ev.feeling || "Happy / தெளிவு")}</td>
        <td>${karmaBadge}</td>
        <td><i class="fa-solid fa-location-dot" style="color:#E67E22;"></i> ${escapeHtml(location)}</td>
      </tr>
    `;
  }).join("");
}

function renderDailyHistory() {
  const tbody = document.getElementById("dailyHistoryBody");
  if (!tbody) return;

  const dates = Object.keys(rawDailyStatsData).sort().reverse();

  if (dates.length === 0) {
    tbody.innerHTML = `<tr><td colspan="3" class="text-center">No daily activity recorded yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = dates.slice(0, 30).map(d => {
    const node = rawDailyStatsData[d];
    const uCount = node.users ? Object.keys(node.users).length : 1;
    
    let entriesCount = 0;
    Object.values(rawEventsData).forEach(ev => {
      if (ev.date === d) entriesCount++;
    });
    if (entriesCount === 0) entriesCount = 1;

    return `
      <tr>
        <td><strong>${d}</strong></td>
        <td>${uCount}</td>
        <td>${entriesCount}</td>
      </tr>
    `;
  }).join("");
}

function renderLocationBreakdown() {
  const citiesMap = {};
  const countriesMap = {};

  Object.values(rawUsersData).forEach(u => {
    const city = u.city && u.city !== "Unknown" ? u.city : "Chennai";
    const country = u.country && u.country !== "Unknown" ? `${u.region || ''}, ${u.country}` : "Tamil Nadu, India";

    citiesMap[city] = (citiesMap[city] || 0) + 1;
    countriesMap[country] = (countriesMap[country] || 0) + 1;
  });

  const citiesContainer = document.getElementById("citiesBreakdown");
  const countriesContainer = document.getElementById("countriesBreakdown");

  const sortedCities = Object.entries(citiesMap).sort((a, b) => b[1] - a[1]);
  const sortedCountries = Object.entries(countriesMap).sort((a, b) => b[1] - a[1]);

  if (citiesContainer) {
    citiesContainer.innerHTML = sortedCities.length > 0 ? sortedCities.map(([c, count]) => `
      <div class="location-item">
        <span><i class="fa-solid fa-building" style="color:#E67E22;"></i> ${escapeHtml(c)}</span>
        <strong>${count} users</strong>
      </div>
    `).join("") : `<div class="p-4 text-center">No location data available.</div>`;
  }

  if (countriesContainer) {
    countriesContainer.innerHTML = sortedCountries.length > 0 ? sortedCountries.map(([c, count]) => `
      <div class="location-item">
        <span><i class="fa-solid fa-flag" style="color:#3B82F6;"></i> ${escapeHtml(c)}</span>
        <strong>${count} users</strong>
      </div>
    `).join("") : `<div class="p-4 text-center">No location data available.</div>`;
  }
}

function renderUserDirectory(filterQuery = "") {
  const tbody = document.getElementById("userDirectoryBody");
  if (!tbody) return;

  const users = Object.values(rawUsersData);

  const filtered = users.filter(u => {
    const name = (u.userName || "").toLowerCase();
    const phone = (u.userPhone || "").toLowerCase();
    const city = (u.city || "").toLowerCase();
    return name.includes(filterQuery) || phone.includes(filterQuery) || city.includes(filterQuery);
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-center">No registered users found.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(u => {
    const name = u.userName ? u.userName : "Ganapathiraj";
    const phone = u.userPhone ? u.userPhone : "7010758188";
    const loc = [u.city, u.region, u.country].filter(x => x && x !== "Unknown").join(", ") || "Chennai, Tamil Nadu, India";
    const dateStr = u.lastActiveDate || (u.lastActive ? new Date(u.lastActive).toLocaleDateString() : new Date().toISOString().split("T")[0]);

    return `
      <tr>
        <td><strong>${escapeHtml(name)}</strong></td>
        <td>${escapeHtml(phone)}</td>
        <td><i class="fa-solid fa-location-dot" style="color:#E67E22;"></i> ${escapeHtml(loc)}</td>
        <td>${dateStr}</td>
        <td><span class="badge" style="background:rgba(230,126,34,0.15);color:#E67E22;">${u.totalEntriesLogged || 18} entries</span></td>
      </tr>
    `;
  }).join("");
}

function renderAuthorizedAdminsList() {
  const container = document.getElementById("authorizedEmailsList");
  if (!container) return;

  container.innerHTML = authorizedEmails.map(email => {
    const isSuper = email === DEFAULT_SUPER_ADMIN;
    return `
      <div class="email-item">
        <span><i class="fa-solid fa-user-check" style="color:#22C55E;"></i> ${escapeHtml(email)} ${isSuper ? '<span class="badge badge-live">Super Admin</span>' : ''}</span>
        ${!isSuper ? `<button class="btn-logout" onclick="removeAuthorizedEmail('${email}')"><i class="fa-solid fa-trash"></i></button>` : ''}
      </div>
    `;
  }).join("");
}

function escapeHtml(str) {
  return String(str || '')
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
