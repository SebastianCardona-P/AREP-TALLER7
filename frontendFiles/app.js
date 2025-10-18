// ===== API Configuration =====
const API_BASE_URL =
  "https://4dk4r5wro9.execute-api.us-east-1.amazonaws.com/beta/api";

// ===== Authentication State =====
let authToken = null;
let isAuthenticated = false;

// ===== Global State =====
let currentUser = null;
let users = [];
let streams = [];
let posts = [];

// ===== Authentication Functions =====

// Load token from localStorage on startup
function loadAuthToken() {
  authToken = localStorage.getItem("idToken");
  isAuthenticated = authToken !== null;
  return isAuthenticated;
}

// Save token to localStorage
function saveAuthToken(token) {
  authToken = token;
  localStorage.setItem("idToken", token);
  isAuthenticated = true;
}

// Clear token
function clearAuthToken() {
  authToken = null;
  localStorage.removeItem("idToken");
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  isAuthenticated = false;
}

// Authenticated fetch helper
async function authenticatedFetch(url, options = {}) {
  if (!authToken) {
    throw new Error("No está autenticado. Por favor, inicie sesión.");
  }

  const headers = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${authToken}`,
    ...options.headers,
  };

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401 || response.status === 403) {
    // Token expired or invalid
    clearAuthToken();
    showToast("Sesión expirada. Por favor, inicie sesión nuevamente.", "error");
    throw new Error("Session expired");
  }

  return response;
}

// ===== Initialize App =====
document.addEventListener("DOMContentLoaded", () => {
  initializeApp();
  setupEventListeners();
});

async function initializeApp() {
  // Load auth token from localStorage
  loadAuthToken();

  if (!isAuthenticated) {
    showToast(
      "Por favor configure el JWT token. Use localStorage.setItem('idToken', 'su_token_aqui')",
      "warning"
    );
    console.warn("No JWT token found. Please authenticate first.");
    // Continuar sin token para permitir configuración manual
  }

  showLoading();
  try {
    await Promise.all([loadUsers(), loadStreams(), loadPosts()]);
    updateStats();
    loadRecentContent();
  } catch (error) {
    if (
      error.message.includes("autenticado") ||
      error.message.includes("Session expired")
    ) {
      showToast("Error de autenticación. Configure su JWT token.", "error");
    } else {
      showToast("Error al cargar datos iniciales", "error");
    }
    console.error(error);
  } finally {
    hideLoading();
  }
}

// ===== Event Listeners =====
function setupEventListeners() {
  // Navigation
  document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const section = btn.dataset.section;
      navigateToSection(section);
    });
  });

  // Forms
  document
    .getElementById("createUserForm")
    .addEventListener("submit", handleCreateUser);
  document
    .getElementById("createStreamForm")
    .addEventListener("submit", handleCreateStream);
  document
    .getElementById("createPostForm")
    .addEventListener("submit", handleCreatePost);

  // Search and filters
  document
    .getElementById("stream-search")
    .addEventListener("input", filterStreams);
  document
    .getElementById("stream-user-filter")
    .addEventListener("change", filterStreams);
  document.getElementById("user-search").addEventListener("input", filterUsers);

  // Character counter for posts
  document
    .getElementById("post-content")
    .addEventListener("input", updateCharCounter);

  // Close modals on background click
  document.querySelectorAll(".modal").forEach((modal) => {
    modal.addEventListener("click", (e) => {
      if (e.target === modal) {
        closeModal(modal.id);
      }
    });
  });
}

// ===== Navigation =====
function navigateToSection(sectionId) {
  // Update nav buttons
  document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.classList.remove("active");
  });
  document
    .querySelector(`[data-section="${sectionId}"]`)
    .classList.add("active");

  // Update content sections
  document.querySelectorAll(".content-section").forEach((section) => {
    section.classList.remove("active");
  });
  document.getElementById(sectionId).classList.add("active");

  // Load section-specific data
  if (sectionId === "streams") {
    loadStreamsSection();
  } else if (sectionId === "users") {
    loadUsersSection();
  } else if (sectionId === "my-profile") {
    loadProfileSection();
  }
}

// ===== API Calls =====

// Users
async function loadUsers() {
  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/users`);
    users = await response.json();
    return users;
  } catch (error) {
    console.error("Error loading users:", error);
    throw error;
  }
}

async function createUser(userData) {
  const response = await authenticatedFetch(`${API_BASE_URL}/users`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error);
  }
  return await response.json();
}

async function deleteUser(userId) {
  const response = await authenticatedFetch(`${API_BASE_URL}/users/${userId}`, {
    method: "DELETE",
  });
  return response.ok;
}

// Streams
async function loadStreams() {
  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/streams`);
    streams = await response.json();
    return streams;
  } catch (error) {
    console.error("Error loading streams:", error);
    throw error;
  }
}

async function createStream(streamData) {
  const response = await authenticatedFetch(`${API_BASE_URL}/streams`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(streamData),
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error);
  }
  return await response.json();
}

async function deleteStream(streamId) {
  const response = await authenticatedFetch(
    `${API_BASE_URL}/streams/${streamId}`,
    {
      method: "DELETE",
    }
  );
  return response.ok;
}

async function getStreamPosts(streamId) {
  const response = await authenticatedFetch(
    `${API_BASE_URL}/posts/stream/${streamId}`
  );
  return await response.json();
}

async function getStreamInfo(streamId) {
  const response = await authenticatedFetch(
    `${API_BASE_URL}/streams/${streamId}`
  );
  return await response.json();
}

// Posts
async function loadPosts() {
  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/posts`);
    posts = await response.json();
    return posts;
  } catch (error) {
    console.error("Error loading posts:", error);
    throw error;
  }
}

async function createPost(postData) {
  const response = await authenticatedFetch(`${API_BASE_URL}/posts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(postData),
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error);
  }
  return await response.json();
}

async function deletePost(postId) {
  const response = await authenticatedFetch(`${API_BASE_URL}/posts/${postId}`, {
    method: "DELETE",
  });
  return response.ok;
}

async function getPostsByUser(userId) {
  const response = await authenticatedFetch(
    `${API_BASE_URL}/posts/user/${userId}`
  );
  return await response.json();
}

async function getPostsByStream(streamId) {
  const response = await authenticatedFetch(
    `${API_BASE_URL}/posts/stream/${streamId}`
  );
  return await response.json();
}

// ===== Form Handlers =====

async function handleCreateUser(e) {
  e.preventDefault();
  showLoading();

  try {
    const userData = {
      username: document.getElementById("username").value.trim(),
      email: document.getElementById("email").value.trim(),
      fullName: document.getElementById("fullName").value.trim() || null,
      bio: document.getElementById("bio").value.trim() || null,
    };

    await createUser(userData);
    await loadUsers();
    loadUsersSection();
    populateUserSelects();

    closeModal("createUserModal");
    e.target.reset();
    showToast("Usuario creado exitosamente", "success");
  } catch (error) {
    showToast(error.message || "Error al crear usuario", "error");
  } finally {
    hideLoading();
  }
}

async function handleCreateStream(e) {
  e.preventDefault();
  showLoading();

  try {
    const streamData = {
      createdBy: document.getElementById("stream-creator").value,
      title: document.getElementById("stream-title").value.trim(),
      description:
        document.getElementById("stream-description").value.trim() || null,
    };

    await createStream(streamData);
    await loadStreams();
    loadStreamsSection();
    populateStreamSelects();

    closeModal("createStreamModal");
    e.target.reset();
    showToast("Stream creado exitosamente", "success");
  } catch (error) {
    showToast(error.message || "Error al crear stream", "error");
  } finally {
    hideLoading();
  }
}

async function handleCreatePost(e) {
  e.preventDefault();
  showLoading();

  try {
    const postData = {
      userId: document.getElementById("post-user").value,
      streamId: document.getElementById("post-stream").value,
      content: document.getElementById("post-content").value.trim(),
    };

    await createPost(postData);
    await loadPosts();
    await loadStreams(); // Reload to update timestamps
    loadRecentContent();

    closeModal("createPostModal");
    e.target.reset();
    updateCharCounter();
    showToast("Post publicado exitosamente", "success");
  } catch (error) {
    showToast(error.message || "Error al crear post", "error");
  } finally {
    hideLoading();
  }
}

// ===== UI Updates =====

function updateStats() {
  document.getElementById("total-users").textContent = users.length;
  document.getElementById("total-streams").textContent = streams.length;
  document.getElementById("total-posts").textContent = posts.length;
}

function loadRecentContent() {
  // Recent streams
  const recentStreamsContainer = document.getElementById("recent-streams");
  const recentStreams = [...streams].slice(0, 5);

  if (recentStreams.length === 0) {
    recentStreamsContainer.innerHTML =
      '<p class="empty-state">No hay streams disponibles</p>';
  } else {
    recentStreamsContainer.innerHTML = recentStreams
      .map((stream) => {
        const creator = users.find((u) => u.id === stream.createdBy);
        const postCount = posts.filter((p) => p.streamId === stream.id).length;

        return `
                <div class="stream-card" onclick="viewStreamDetail('${
                  stream.id
                }')">
                    <div class="stream-card-header">
                        <div class="stream-card-title">
                            <h3>${escapeHtml(stream.title)}</h3>
                            <div class="stream-card-meta">
                                <i class="fas fa-user"></i>
                                <span>${
                                  creator
                                    ? escapeHtml(creator.username)
                                    : "Desconocido"
                                }</span>
                            </div>
                        </div>
                    </div>
                    ${
                      stream.description
                        ? `<p class="stream-card-description">${escapeHtml(
                            stream.description
                          )}</p>`
                        : ""
                    }
                    <div class="stream-card-stats">
                        <div class="stream-stat">
                            <i class="fas fa-comments"></i>
                            <span>${postCount} posts</span>
                        </div>
                        <div class="stream-stat">
                            <i class="fas fa-clock"></i>
                            <span>${formatDate(stream.createdAt)}</span>
                        </div>
                    </div>
                </div>
            `;
      })
      .join("");
  }

  // Recent posts
  const recentPostsContainer = document.getElementById("recent-posts");
  const recentPosts = [...posts].slice(0, 8);

  if (recentPosts.length === 0) {
    recentPostsContainer.innerHTML =
      '<p class="empty-state">No hay posts disponibles</p>';
  } else {
    recentPostsContainer.innerHTML = recentPosts
      .map((post) => {
        const author = users.find((u) => u.id === post.userId);
        const stream = streams.find((s) => s.id === post.streamId);

        return `
                <div class="post-card">
                    <div class="post-header">
                        <div class="post-author">
                            <div class="post-author-avatar">${getInitials(
                              author?.username || "U"
                            )}</div>
                            <div class="post-author-info">
                                <span class="post-author-name">${escapeHtml(
                                  author?.username || "Desconocido"
                                )}</span>
                                <span class="post-date">${formatDate(
                                  post.createdAt
                                )}</span>
                            </div>
                        </div>
                    </div>
                    <div class="post-content">${escapeHtml(post.content)}</div>
                    <div class="post-footer">
                        <span class="post-stream-tag">
                            <i class="fas fa-stream"></i>
                            ${escapeHtml(stream?.title || "Stream eliminado")}
                        </span>
                    </div>
                </div>
            `;
      })
      .join("");
  }
}

function loadStreamsSection() {
  populateUserSelects();
  populateUserFilter();
  filterStreams();
}

function filterStreams() {
  const searchTerm = document
    .getElementById("stream-search")
    .value.toLowerCase();
  const userFilter = document.getElementById("stream-user-filter").value;

  let filtered = streams;

  if (searchTerm) {
    filtered = filtered.filter(
      (s) =>
        s.title.toLowerCase().includes(searchTerm) ||
        (s.description && s.description.toLowerCase().includes(searchTerm))
    );
  }

  if (userFilter) {
    filtered = filtered.filter((s) => s.createdBy === userFilter);
  }

  displayStreams(filtered);
}

function displayStreams(streamsToDisplay) {
  const container = document.getElementById("streams-list");

  if (streamsToDisplay.length === 0) {
    container.innerHTML =
      '<p class="empty-state"><i class="fas fa-inbox"></i><br>No se encontraron streams</p>';
    return;
  }

  container.innerHTML = streamsToDisplay
    .map((stream) => {
      const creator = users.find((u) => u.id === stream.createdBy);
      const postCount = posts.filter((p) => p.streamId === stream.id).length;

      return `
            <div class="stream-card">
                <div class="stream-card-header">
                    <div class="stream-card-title">
                        <h3>${escapeHtml(stream.title)}</h3>
                        <div class="stream-card-meta">
                            <i class="fas fa-user"></i>
                            <span>${
                              creator
                                ? escapeHtml(creator.username)
                                : "Desconocido"
                            }</span>
                        </div>
                    </div>
                </div>
                ${
                  stream.description
                    ? `<p class="stream-card-description">${escapeHtml(
                        stream.description
                      )}</p>`
                    : ""
                }
                <div class="stream-card-stats">
                    <div class="stream-stat">
                        <i class="fas fa-comments"></i>
                        <span>${postCount} posts</span>
                    </div>
                    <div class="stream-stat">
                        <i class="fas fa-clock"></i>
                        <span>${formatDate(stream.createdAt)}</span>
                    </div>
                </div>
                <div class="stream-card-actions">
                    <button class="btn-primary" onclick="viewStreamDetail('${
                      stream.id
                    }')">
                        <i class="fas fa-eye"></i> Ver Detalles
                    </button>
                    <button class="btn-primary" onclick="showCreatePostModalForStream('${
                      stream.id
                    }')">
                        <i class="fas fa-plus"></i> Nuevo Post
                    </button>
                    <button class="btn-danger" onclick="handleDeleteStream('${
                      stream.id
                    }')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    })
    .join("");
}

function loadUsersSection() {
  populateUserSelects();
  filterUsers();
}

function filterUsers() {
  const searchTerm = document.getElementById("user-search").value.toLowerCase();

  const filtered = users.filter(
    (u) =>
      u.username.toLowerCase().includes(searchTerm) ||
      u.email.toLowerCase().includes(searchTerm) ||
      (u.fullName && u.fullName.toLowerCase().includes(searchTerm))
  );

  displayUsers(filtered);
}

function displayUsers(usersToDisplay) {
  const container = document.getElementById("users-list");

  if (usersToDisplay.length === 0) {
    container.innerHTML =
      '<p class="empty-state"><i class="fas fa-user-slash"></i><br>No se encontraron usuarios</p>';
    return;
  }

  container.innerHTML = usersToDisplay
    .map((user) => {
      const userPosts = posts.filter((p) => p.userId === user.id);
      const userStreams = streams.filter((s) => s.createdBy === user.id);

      return `
            <div class="user-card">
                <div class="user-card-header">
                    <div class="user-avatar">${getInitials(user.username)}</div>
                    <div class="user-info">
                        <h3>${escapeHtml(user.username)}</h3>
                        <p>${escapeHtml(user.email)}</p>
                        ${
                          user.fullName
                            ? `<p><i class="fas fa-id-card"></i> ${escapeHtml(
                                user.fullName
                              )}</p>`
                            : ""
                        }
                    </div>
                </div>
                ${
                  user.bio
                    ? `<p class="user-bio">${escapeHtml(user.bio)}</p>`
                    : ""
                }
                <div class="user-card-footer">
                    <span class="user-date">
                        <i class="fas fa-calendar"></i> ${formatDate(
                          user.createdAt
                        )}
                    </span>
                    <div class="user-actions">
                        <button class="btn-icon" onclick="viewUserStreams('${
                          user.id
                        }')" title="Ver streams">
                            <i class="fas fa-stream"></i> ${userStreams.length}
                        </button>
                        <button class="btn-icon" onclick="viewUserPosts('${
                          user.id
                        }')" title="Ver posts">
                            <i class="fas fa-comments"></i> ${userPosts.length}
                        </button>
                        <button class="btn-danger" onclick="handleDeleteUser('${
                          user.id
                        }')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    })
    .join("");
}

function loadProfileSection() {
  const select = document.getElementById("current-user-select");
  select.innerHTML =
    '<option value="">-- Selecciona un usuario --</option>' +
    users
      .map((u) => `<option value="${u.id}">${escapeHtml(u.username)}</option>`)
      .join("");
}

async function loadUserProfile() {
  const userId = document.getElementById("current-user-select").value;
  const container = document.getElementById("profile-content");

  if (!userId) {
    container.innerHTML =
      '<p class="empty-state">Selecciona un usuario para ver su perfil</p>';
    return;
  }

  showLoading();

  try {
    const user = users.find((u) => u.id === userId);
    const userPosts = await getPostsByUser(userId);
    const userStreams = streams.filter((s) => s.createdBy === userId);

    container.innerHTML = `
            <div class="profile-header">
                <div class="profile-avatar-large">${getInitials(
                  user.username
                )}</div>
                <div class="profile-info">
                    <h2>${escapeHtml(user.username)}</h2>
                    <p>${escapeHtml(user.email)}</p>
                    ${
                      user.fullName
                        ? `<p><strong>${escapeHtml(user.fullName)}</strong></p>`
                        : ""
                    }
                    ${
                      user.bio
                        ? `<p class="profile-bio">${escapeHtml(user.bio)}</p>`
                        : ""
                    }
                </div>
            </div>
            
            <div class="profile-stats">
                <div class="profile-stat-card">
                    <i class="fas fa-stream"></i>
                    <h3>${userStreams.length}</h3>
                    <p>Streams Creados</p>
                </div>
                <div class="profile-stat-card">
                    <i class="fas fa-comments"></i>
                    <h3>${userPosts.length}</h3>
                    <p>Posts Publicados</p>
                </div>
                <div class="profile-stat-card">
                    <i class="fas fa-calendar"></i>
                    <h3>${formatDate(user.createdAt)}</h3>
                    <p>Miembro desde</p>
                </div>
            </div>
            
            <div class="profile-section">
                <h3><i class="fas fa-stream"></i> Mis Streams</h3>
                ${
                  userStreams.length > 0
                    ? `<div class="streams-grid">
                        ${userStreams
                          .map((stream) => {
                            const postCount = posts.filter(
                              (p) => p.streamId === stream.id
                            ).length;
                            return `
                                <div class="stream-card" onclick="viewStreamDetail('${
                                  stream.id
                                }')">
                                    <h3>${escapeHtml(stream.title)}</h3>
                                    ${
                                      stream.description
                                        ? `<p class="stream-card-description">${escapeHtml(
                                            stream.description
                                          )}</p>`
                                        : ""
                                    }
                                    <div class="stream-card-stats">
                                        <div class="stream-stat">
                                            <i class="fas fa-comments"></i>
                                            <span>${postCount} posts</span>
                                        </div>
                                    </div>
                                </div>
                            `;
                          })
                          .join("")}
                    </div>`
                    : '<p class="empty-state">No has creado ningún stream</p>'
                }
            </div>
            
            <div class="profile-section">
                <h3><i class="fas fa-comments"></i> Mis Posts</h3>
                ${
                  userPosts.length > 0
                    ? userPosts
                        .map((post) => {
                          const stream = streams.find(
                            (s) => s.id === post.streamId
                          );
                          return `
                            <div class="post-card">
                                <div class="post-header">
                                    <div class="post-author">
                                        <div class="post-author-avatar">${getInitials(
                                          user.username
                                        )}</div>
                                        <div class="post-author-info">
                                            <span class="post-author-name">${escapeHtml(
                                              user.username
                                            )}</span>
                                            <span class="post-date">${formatDate(
                                              post.createdAt
                                            )}</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="post-content">${escapeHtml(
                                  post.content
                                )}</div>
                                <div class="post-footer">
                                    <span class="post-stream-tag">
                                        <i class="fas fa-stream"></i>
                                        ${escapeHtml(
                                          stream?.title || "Stream eliminado"
                                        )}
                                    </span>
                                    <button class="btn-danger" onclick="handleDeletePost('${
                                      post.id
                                    }')">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </div>
                        `;
                        })
                        .join("")
                    : '<p class="empty-state">No has publicado ningún post</p>'
                }
            </div>
        `;
  } catch (error) {
    showToast("Error al cargar perfil", "error");
  } finally {
    hideLoading();
  }
}

async function viewStreamDetail(streamId) {
  showLoading();

  try {
    const stream = await getStreamInfo(streamId);
    const streamPosts = await getStreamPosts(streamId);
    const creator = users.find((u) => u.id === stream.createdBy);

    document.getElementById("stream-detail-title").innerHTML = `
            <i class="fas fa-stream"></i> ${escapeHtml(stream.title)}
        `;

    document.getElementById("stream-detail-content").innerHTML = `
            <div style="margin-bottom: 25px;">
                <div class="stream-card-meta" style="margin-bottom: 10px;">
                    <i class="fas fa-user"></i>
                    <span>Creado por: <strong>${escapeHtml(
                      creator?.username || "Desconocido"
                    )}</strong></span>
                </div>
                ${
                  stream.description
                    ? `<p style="color: var(--color-text-light); margin-bottom: 10px;">${escapeHtml(
                        stream.description
                      )}</p>`
                    : ""
                }
                <div style="display: flex; gap: 20px; color: var(--color-text-muted); font-size: 0.9rem;">
                    <span><i class="fas fa-comments"></i> ${
                      streamPosts.length
                    } posts</span>
                    <span><i class="fas fa-clock"></i> ${formatDate(
                      stream.createdAt
                    )}</span>
                </div>
            </div>
            
            <div style="margin-bottom: 20px;">
                <button class="btn-primary" onclick="showCreatePostModalForStream('${streamId}')">
                    <i class="fas fa-plus"></i> Agregar Post
                </button>
            </div>
            
            <h4 style="color: var(--color-primary); margin-bottom: 15px;">
                <i class="fas fa-comments"></i> Posts en este Stream
            </h4>
            
            ${
              streamPosts.length > 0
                ? streamPosts
                    .map((post) => {
                      const author = users.find((u) => u.id === post.userId);
                      return `
                        <div class="post-card">
                            <div class="post-header">
                                <div class="post-author">
                                    <div class="post-author-avatar">${getInitials(
                                      author?.username || "U"
                                    )}</div>
                                    <div class="post-author-info">
                                        <span class="post-author-name">${escapeHtml(
                                          author?.username || "Desconocido"
                                        )}</span>
                                        <span class="post-date">${formatDate(
                                          post.createdAt
                                        )}</span>
                                    </div>
                                </div>
                                <button class="btn-danger" onclick="handleDeletePostInModal('${
                                  post.id
                                }', '${streamId}')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                            <div class="post-content">${escapeHtml(
                              post.content
                            )}</div>
                        </div>
                    `;
                    })
                    .join("")
                : '<p class="empty-state">No hay posts en este stream todavía</p>'
            }
        `;

    showModal("streamDetailModal");
  } catch (error) {
    showToast("Error al cargar detalles del stream", "error");
  } finally {
    hideLoading();
  }
}

function viewUserStreams(userId) {
  navigateToSection("streams");
  document.getElementById("stream-user-filter").value = userId;
  filterStreams();
}

function viewUserPosts(userId) {
  navigateToSection("my-profile");
  document.getElementById("current-user-select").value = userId;
  loadUserProfile();
}

// ===== Delete Handlers =====

async function handleDeleteUser(userId) {
  if (
    !confirm(
      "¿Estás seguro de eliminar este usuario? Esto también eliminará sus streams y posts."
    )
  ) {
    return;
  }

  showLoading();

  try {
    await deleteUser(userId);
    await Promise.all([loadUsers(), loadStreams(), loadPosts()]);
    loadUsersSection();
    updateStats();
    showToast("Usuario eliminado exitosamente", "success");
  } catch (error) {
    showToast("Error al eliminar usuario", "error");
  } finally {
    hideLoading();
  }
}

async function handleDeleteStream(streamId) {
  if (
    !confirm(
      "¿Estás seguro de eliminar este stream? Esto también eliminará todos sus posts."
    )
  ) {
    return;
  }

  showLoading();

  try {
    await deleteStream(streamId);
    await Promise.all([loadStreams(), loadPosts()]);
    loadStreamsSection();
    updateStats();
    showToast("Stream eliminado exitosamente", "success");
  } catch (error) {
    showToast("Error al eliminar stream", "error");
  } finally {
    hideLoading();
  }
}

async function handleDeletePost(postId) {
  if (!confirm("¿Estás seguro de eliminar este post?")) {
    return;
  }

  showLoading();

  try {
    await deletePost(postId);
    await loadPosts();
    loadUserProfile();
    updateStats();
    showToast("Post eliminado exitosamente", "success");
  } catch (error) {
    showToast("Error al eliminar post", "error");
  } finally {
    hideLoading();
  }
}

async function handleDeletePostInModal(postId, streamId) {
  if (!confirm("¿Estás seguro de eliminar este post?")) {
    return;
  }

  showLoading();

  try {
    await deletePost(postId);
    await loadPosts();
    viewStreamDetail(streamId); // Reload modal
    updateStats();
    showToast("Post eliminado exitosamente", "success");
  } catch (error) {
    showToast("Error al eliminar post", "error");
  } finally {
    hideLoading();
  }
}

// ===== Modal Functions =====

function showModal(modalId) {
  document.getElementById(modalId).classList.add("active");
}

function closeModal(modalId) {
  document.getElementById(modalId).classList.remove("active");
}

function showCreateUserModal() {
  showModal("createUserModal");
}

function showCreateStreamModal() {
  populateUserSelects();
  showModal("createStreamModal");
}

function showCreatePostModalForStream(streamId) {
  populateUserSelects();
  populateStreamSelects();
  if (streamId) {
    document.getElementById("post-stream").value = streamId;
  }
  closeModal("streamDetailModal");
  showModal("createPostModal");
}

// ===== Helper Functions =====

function populateUserSelects() {
  const selects = ["stream-creator", "post-user", "current-user-select"];
  selects.forEach((selectId) => {
    const select = document.getElementById(selectId);
    if (select) {
      const currentValue = select.value;
      select.innerHTML =
        '<option value="">-- Selecciona un usuario --</option>' +
        users
          .map(
            (u) => `<option value="${u.id}">${escapeHtml(u.username)}</option>`
          )
          .join("");
      if (currentValue) {
        select.value = currentValue;
      }
    }
  });
}

function populateStreamSelects() {
  const select = document.getElementById("post-stream");
  select.innerHTML =
    '<option value="">-- Selecciona un stream --</option>' +
    streams
      .map((s) => `<option value="${s.id}">${escapeHtml(s.title)}</option>`)
      .join("");
}

function populateUserFilter() {
  const select = document.getElementById("stream-user-filter");
  select.innerHTML =
    '<option value="">Todos los usuarios</option>' +
    users
      .map((u) => `<option value="${u.id}">${escapeHtml(u.username)}</option>`)
      .join("");
}

function updateCharCounter() {
  const content = document.getElementById("post-content").value;
  document.getElementById("char-count").textContent = content.length;
}

function getInitials(name) {
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase()
    .substring(0, 2);
}

function formatDate(dateString) {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);

  if (diffMins < 1) return "Ahora";
  if (diffMins < 60) return `Hace ${diffMins}m`;

  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `Hace ${diffHours}h`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `Hace ${diffDays}d`;

  return date.toLocaleDateString("es-ES", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

function showLoading() {
  document.getElementById("loading").style.display = "flex";
}

function hideLoading() {
  document.getElementById("loading").style.display = "none";
}

function showToast(message, type = "info") {
  const container = document.getElementById("toast-container");
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;

  const icons = {
    success: "fa-check-circle",
    error: "fa-exclamation-circle",
    warning: "fa-exclamation-triangle",
    info: "fa-info-circle",
  };

  toast.innerHTML = `
        <i class="fas ${icons[type]}"></i>
        <span class="toast-message">${escapeHtml(message)}</span>
    `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = "slideOutRight 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}
