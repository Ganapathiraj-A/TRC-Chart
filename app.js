/* ==========================================================================
   TRC Analytics Dashboard - App Core Logic & Firebase Integration
   ========================================================================== */

const FIREBASE_CONFIG = {
  databaseURL: "https://trc-chart-analytics-default-rtdb.firebaseio.com"
};

// Default Pre-authorized Super Admin Email
const DEFAULT_SUPER_ADMIN = "ganapathiraj@gmail.com";

// Initialize Firebase
if (!firebase.apps.length) {
  firebase.initializeApp(FIREBASE_CONFIG);
}
const db = firebase.database();
const auth = firebase.auth();

// App State
let currentUser = null;
let authorizedEmails = [DEFAULT_SUPER_ADMIN];
let rawUsersData = {};
let rawEventsData = {};
let rawDailyStatsData = {};

// DOM Initialization
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initApp);
} else {
  initApp();
}

function initApp() {
  setupEventListeners();
  initAuth();
}

function switchTab(btnElement, targetTabId) {
  const navBtns = document.querySelectorAll(".nav-btn");
  const tabPages = document.querySelectorAll(".tab-page");
  navBtns.forEach(b => b.classList.remove("active"));
  tabPages.forEach(p => p.classList.add("hidden"));

  if (btnElement) btnElement.classList.add("active");
  const target = document.getElementById(targetTabId);
  if (target) {
    target.classList.remove("hidden");
    target.classList.add("active");
  }
}

function handleGoogleSignIn() {
  handleEmailLogin("ganapathiraj@gmail.com");
}

function handleDirectFormSubmit(e) {
  if (e && e.preventDefault) e.preventDefault();
  const emailInput = document.getElementById("directEmailInput");
  const email = emailInput ? emailInput.value.trim().toLowerCase() : "ganapathiraj@gmail.com";
  handleEmailLogin(email);
}

function setupEventListeners() {
  const googleBtn = document.getElementById("googleSignInBtn");
  if (googleBtn) {
    googleBtn.onclick = handleGoogleSignIn;
  }

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.onclick = () => {
      localStorage.removeItem("trc_dashboard_user");
      showLogin();
    };
  }

  // User Search
  const searchInput = document.getElementById("userSearchInput");
  if (searchInput) {
    searchInput.oninput = (e) => {
      renderUserDirectory(e.target.value.toLowerCase());
    };
  }

  // Add Admin Form
  const adminForm = document.getElementById("addAdminForm");
  if (adminForm) {
    adminForm.onsubmit = (e) => {
      e.preventDefault();
      const emailInput = document.getElementById("adminEmailInput");
      const email = emailInput.value.trim().toLowerCase();
      if (email) {
        addAuthorizedEmail(email);
        emailInput.value = "";
      }
    };
  }
}

function initAuth() {
  // Check stored session
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

async function handleEmailLogin(email) {
  const authError = document.getElementById("authError");
  const authErrorText = document.getElementById("authErrorText");
  if (authError) authError.classList.add("hidden");

  email = (email || "ganapathiraj@gmail.com").toLowerCase();
  
  await fetchAuthorizedEmails();

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
  loginView.classList.remove("hidden");
  dashboardView.classList.add("hidden");
}

function showDashboard(user) {
  loginView.classList.add("hidden");
  dashboardView.classList.remove("hidden");

  userNameDisplay.textContent = user.displayName || "Admin User";
  userEmailDisplay.textContent = user.email;
  if (user.photoURL) userAvatar.src = user.photoURL;
}

// Fetch Authorized Emails
async function fetchAuthorizedEmails() {
  try {
    const snapshot = await db.ref("authorized_users").once("value");
    const val = snapshot.val() || {};
    authorizedEmails = [DEFAULT_SUPER_ADMIN];
    Object.values(val).forEach(item => {
      if (item && item.email) authorizedEmails.push(item.email.toLowerCase());
    });
  } catch (e) {
    authorizedEmails = [DEFAULT_SUPER_ADMIN];
  }
}

function addAuthorizedEmail(email) {
  const key = email.replace(/[\.\#\$\[\]]/g, "_");
  db.ref(`authorized_users/${key}`).set({
    email: email,
    addedBy: currentUser.email,
    addedAt: Date.now()
  }).then(() => {
    alert(`Successfully authorized ${email}!`);
  });
}

function removeAuthorizedEmail(email) {
  if (email === DEFAULT_SUPER_ADMIN) {
    alert("Super Admin ganapathiraj@gmail.com cannot be removed!");
    return;
  }
  const key = email.replace(/[\.\#\$\[\]]/g, "_");
  db.ref(`authorized_users/${key}`).remove().then(() => {
    alert(`Removed ${email} from authorized admins.`);
  });
}

// Real-time Data Listeners
function startDataSync() {
  // Listen to Users Node
  db.ref("users").on("value", snapshot => {
    rawUsersData = snapshot.val() || {};
    updateDashboardMetrics();
    renderUserDirectory();
    renderLocationBreakdown();
  });

  // Listen to Daily Stats Node
  db.ref("daily_stats").on("value", snapshot => {
    rawDailyStatsData = snapshot.val() || {};
    updateDashboardMetrics();
    renderDailyHistory();
  });

  // Listen to Real-time Events Node
  db.ref("events").limitToLast(50).on("value", snapshot => {
    rawEventsData = snapshot.val() || {};
    updateDashboardMetrics();
    renderLiveEvents();
  });

  // Listen to Authorized Admins Node
  db.ref("authorized_users").on("value", snapshot => {
    const val = snapshot.val() || {};
    authorizedEmails = [DEFAULT_SUPER_ADMIN];
    Object.values(val).forEach(item => {
      if (item && item.email) authorizedEmails.push(item.email.toLowerCase());
    });
    renderAuthorizedAdminsList();
  });
}

// Calculate & Render All Statistics
function updateDashboardMetrics() {
  const now = new Date();
  const todayStr = now.toISOString().split("T")[0];

  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  const yesterdayStr = yesterday.toISOString().split("T")[0];

  // 1. Today's Snapshot
  const todayNode = rawDailyStatsData[todayStr] || {};
  const todayUsersCount = todayNode.users ? Object.keys(todayNode.users).length : 0;

  let todayWritesCount = 0;
  Object.values(rawEventsData).forEach(ev => {
    if (ev.date === todayStr) todayWritesCount++;
  });

  document.getElementById("snapshotDateLabel").textContent = `Today's Snapshot (${todayStr})`;
  document.getElementById("snapTodayUsers").textContent = todayUsersCount;
  document.getElementById("snapTodayReads").textContent = todayWritesCount * 2 + 10; // Read scaling estimation
  document.getElementById("snapTodayWrites").textContent = todayWritesCount;

  // 2. Usage Scaling Metrics
  const userKeys = Object.keys(rawUsersData);
  const lifetimeReach = userKeys.length;

  const datesList = Object.keys(rawDailyStatsData).sort().reverse();
  const last30Dates = datesList.slice(0, 30);

  let totalDailyUsers30d = 0;
  let peakUsers30d = 0;

  last30Dates.forEach(d => {
    const uCount = rawDailyStatsData[d].users ? Object.keys(rawDailyStatsData[d].users).length : 0;
    totalDailyUsers30d += uCount;
    if (uCount > peakUsers30d) peakUsers30d = uCount;
  });

  const avgUsers30d = last30Dates.length > 0 ? Math.round(totalDailyUsers30d / last30Dates.length) : 0;

  document.getElementById("metricAvgUsers").textContent = avgUsers30d;
  document.getElementById("metricPeakUsers").textContent = peakUsers30d;
  document.getElementById("metricActiveAudience").textContent = userKeys.filter(k => {
    const u = rawUsersData[k];
    return u.lastActive && (now.getTime() - u.lastActive) <= (30 * 24 * 60 * 60 * 1000);
  }).length;
  document.getElementById("metricLifetimeReach").textContent = lifetimeReach;
}

// Render Live Feelings Events
function renderLiveEvents() {
  const tbody = document.getElementById("liveEventsBody");
  const events = Object.values(rawEventsData).reverse();

  if (events.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-center">No feelings logged yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = events.slice(0, 15).map(ev => {
    const timeStr = new Date(ev.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const location = [ev.city, ev.region].filter(x => x && x !== "Unknown").join(", ") || "India";
    const karmaBadge = ev.isGoodKarma ? `<span class="badge" style="background:rgba(34,197,94,0.15);color:#22C55E;">Good Karma</span>` : `<span class="badge" style="background:rgba(239,68,68,0.15);color:#EF4444;">Bad Karma</span>`;
    const userName = ev.userName ? ev.userName : (rawUsersData[ev.installationId]?.userName || "Anonymous");

    return `
      <tr>
        <td>${timeStr}</td>
        <td><strong>${escapeHtml(userName)}</strong></td>
        <td>${escapeHtml(ev.feeling || "Feeling")}</td>
        <td>${karmaBadge}</td>
        <td><i class="fa-solid fa-location-dot" style="color:#E67E22;"></i> ${escapeHtml(location)}</td>
      </tr>
    `;
  }).join("");
}

// Render 30 Days History Table
function renderDailyHistory() {
  const tbody = document.getElementById("dailyHistoryBody");
  const dates = Object.keys(rawDailyStatsData).sort().reverse();

  if (dates.length === 0) {
    tbody.innerHTML = `<tr><td colspan="3" class="text-center">No daily activity recorded yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = dates.slice(0, 30).map(d => {
    const node = rawDailyStatsData[d];
    const uCount = node.users ? Object.keys(node.users).length : 0;
    
    let entriesCount = 0;
    Object.values(rawEventsData).forEach(ev => {
      if (ev.date === d) entriesCount++;
    });

    return `
      <tr>
        <td><strong>${d}</strong></td>
        <td>${uCount}</td>
        <td>${entriesCount}</td>
      </tr>
    `;
  }).join("");
}

// Render Location Breakdown
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

  citiesContainer.innerHTML = sortedCities.length > 0 ? sortedCities.map(([c, count]) => `
    <div class="location-item">
      <span><i class="fa-solid fa-building" style="color:#E67E22;"></i> ${escapeHtml(c)}</span>
      <strong>${count} users</strong>
    </div>
  `).join("") : `<div class="p-4 text-center">No location data available.</div>`;

  countriesContainer.innerHTML = sortedCountries.length > 0 ? sortedCountries.map(([c, count]) => `
    <div class="location-item">
      <span><i class="fa-solid fa-flag" style="color:#3B82F6;"></i> ${escapeHtml(c)}</span>
      <strong>${count} users</strong>
    </div>
  `).join("") : `<div class="p-4 text-center">No location data available.</div>`;
}

// Render User Directory
function renderUserDirectory(filterQuery = "") {
  const tbody = document.getElementById("userDirectoryBody");
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
    const name = u.userName ? u.userName : "Not set";
    const phone = u.userPhone ? u.userPhone : "Not set";
    const loc = [u.city, u.region, u.country].filter(x => x && x !== "Unknown").join(", ") || "India";
    const dateStr = u.lastActiveDate || (u.lastActive ? new Date(u.lastActive).toLocaleDateString() : "N/A");

    return `
      <tr>
        <td><strong>${escapeHtml(name)}</strong></td>
        <td>${escapeHtml(phone)}</td>
        <td><i class="fa-solid fa-location-dot" style="color:#E67E22;"></i> ${escapeHtml(loc)}</td>
        <td>${dateStr}</td>
        <td><span class="badge" style="background:rgba(230,126,34,0.15);color:#E67E22;">${u.totalEntriesLogged || 0} entries</span></td>
      </tr>
    `;
  }).join("");
}

// Render Authorized Admins List
function renderAuthorizedAdminsList() {
  const container = document.getElementById("authorizedEmailsList");
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
