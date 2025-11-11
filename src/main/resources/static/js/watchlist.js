let currentMovieId = null;
const params = new URLSearchParams(window.location.search);
const userId = params.get("userId");

// Set account link
const userPageLink = document.getElementById('account-link');
userPageLink.href = `http://localhost:8080/user/personal?userId=${parseInt(userId)}`;

function goHome() {
    window.location.href = `home?userId=${userId}`;
}

// Tab switching
function showTab(tabName) {
    const tabs = document.querySelectorAll('.tab-content');
    const buttons = document.querySelectorAll('.tab-btn');

    tabs.forEach(tab => tab.classList.remove('active'));
    buttons.forEach(btn => btn.classList.remove('active'));

    document.getElementById(tabName).classList.add('active');
    event.target.closest('.tab-btn').classList.add('active');
}

// API helper
async function fetchApi(url, options = {}) {
    try {
        const res = await fetch(url, options);
        return await res.json();
    } catch (err) {
        console.error('fetchApi error:', err);
        return { status: 'Error', data: { data: [] } };
    }
}

// Render movies
function renderMovies(movies, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    if (!movies || movies.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-film"></i>
                <h3>Chưa có phim nào</h3>
                <p>Hãy khám phá và thêm phim yêu thích của bạn!</p>
            </div>
        `;
        return;
    }

    movies.forEach(movie => {
        const card = document.createElement('div');
        card.className = 'movie-card';
        card.innerHTML = `
            <img src="${movie.thumbUrl}" alt="${movie.title}">
            <div class="overlay">
                <h4>${movie.title}</h4>
                <p>${movie.description || 'Chưa có mô tả'}</p>
            </div>
        `;
        card.onclick = () => showMovieDetail(movie);
        container.appendChild(card);
    });
}

// Load watchlist data
async function loadWatchlist() {
    // Load liked movies
    const likedJson = await fetchApi(`http://localhost:8080/user/get-liked-movies-by-userId?userId=${userId}`);
    renderMovies(likedJson.data?.data, 'liked-movies');

    // Load saved movies
    const savedJson = await fetchApi(`http://localhost:8080/user/get-saved-movies-by-userId?userId=${userId}`);
    renderMovies(savedJson.data?.data, 'saved-movies');

    // Load watched movies
    const watchedJson = await fetchApi(`http://localhost:8080/user/get-watched-movies-by-userId?userId=${userId}`);
    renderMovies(watchedJson.data?.data, 'watched-movies');
}

// Movie detail functions
async function fetchMovieById(movieId) {
    const json = await fetchApi(`http://localhost:8080/user/get-movie?movieId=${movieId}`);
    return json.data?.data || null;
}

async function fetchEpisodes(movieId) {
    const json = await fetchApi(`http://localhost:8080/user/get-episodes?movieId=${movieId}`);
    return json;
}

async function fetchComments(movieId) {
    const json = await fetchApi(`http://localhost:8080/user/get-comments-by-movieId?userId=${userId}&movieId=${movieId}`);
    return json;
}

async function fetchInteraction(movieId, userId) {
    try {
        const res = await fetch(`http://localhost:8080/user/get-movie-interaction?userId=${userId}&movieId=${movieId}`);
        if (!res.ok) throw new Error("HTTP error " + res.status);

        const result = await res.json();
        if (result.status === "Success") {
            const data = result.data.data;
            document.getElementById("like-btn").innerHTML =
                `<i class="fa-solid fa-thumbs-up"></i> ${data.userLikedThis ? "Liked" : "Like"} (${data.totalLikes})`;
            document.getElementById("save-btn").innerHTML =
                `<i class="fa-solid fa-bookmark"></i> ${data.userSavedThis ? "Saved" : "Save"} (${data.totalSaves})`;
            document.getElementById("view-btn").innerHTML =
                `<i class="fa-solid fa-eye"></i> View (${data.totalView})`;
        }
    } catch (err) {
        console.error("fetchInteraction error:", err);
    }
}

async function toggleInteraction(movieId, userId, action) {
    try {
        let interactionUrl = '';
        if (action === "LIKE") {
            interactionUrl = `http://localhost:8080/user/interaction-like?userId=${userId}&movieId=${movieId}`;
        } else if (action === "SAVE") {
            interactionUrl = `http://localhost:8080/user/interaction-save?userId=${userId}&movieId=${movieId}`;
        }

        const res = await fetch(interactionUrl, { method: "PUT" });
        const result = await res.json();

        if (result.status === "Success") {
            await fetchInteraction(movieId, userId);
            // Reload watchlist to reflect changes
            await loadWatchlist();
        }
    } catch (err) {
        console.error("toggleInteraction error:", err);
    }
}

function showMovieDetail(movie) {
    if (!movie) return;
    currentMovieId = movie.movieId;

    document.getElementById('detail-img').src = movie.posterUrl || movie.thumbUrl || '';
    document.getElementById('detail-title').innerText = movie.title || '';
    document.getElementById('detail-desc').innerText = movie.description || '';
    document.getElementById('detail-year').innerText = movie.releaseYear || '';
    document.getElementById('detail-country').innerText = movie.country || '';
    document.getElementById('detail-language').innerText = movie.language || '';
    document.getElementById('detail-status').innerText = movie.movieStatus || '';
    document.getElementById('detail-trailer').href = movie.trailerUrl || '#';

    fetchInteraction(movie.movieId, userId);
    document.getElementById('movie-detail').style.display = 'block';

    // Load episodes
    fetchEpisodes(movie.movieId).then(episodes => {
        const container = document.getElementById('episode-list');
        container.innerHTML = '';

        if (episodes.status === 'Error' || !episodes.data || episodes.data.data.length === 0) {
            container.innerHTML = `
                <div style="color: #666; font-style: italic; text-align: center; margin: 15px 0;">
                    Lỗi tải tập phim! Vui lòng tải lại trang!
                </div>
            `;
            return;
        }

        episodes.data.data.forEach(ep => {
            const div = document.createElement('div');
            div.className = 'episode-card';

            const a = document.createElement('a');
            a.href = ep.videoUrl;
            a.target = "_blank";
            a.textContent = ep.name;

            a.addEventListener('click', async (e) => {
                await fetchApi(`http://localhost:8080/user/interaction-watch?userId=${parseInt(userId)}&movieId=${currentMovieId}`, { method: 'PUT' });
                await fetchInteraction(currentMovieId, parseInt(userId));
            });

            div.appendChild(a);
            container.appendChild(div);
        });
    });

    // Load comments
    fetchComments(movie.movieId).then(comments => {
        const container = document.getElementById('comment-list');
        container.innerHTML = '';

        if (comments.status === "Error" || !comments.data.data || comments.data.data.length === 0) {
            container.innerHTML = '<p class="no-comment">Chưa có bình luận nào. Hãy là người đầu tiên!</p>';
            return;
        }

        comments.data.data.forEach(c => {
            const div = document.createElement('div');
            div.className = 'comment-item';
            div.innerHTML = `
                <div class="comment-header">
                    <span class="comment-user">👤${c.username}</span>
                    <span class="comment-time">${new Date(c.createdAt).toLocaleString('vi-VN')}</span>
                </div>
                <div class="comment-content">${c.content}</div>
            `;
            container.appendChild(div);
        });
    });
}

function closeMovieDetail() {
    document.getElementById('movie-detail').style.display = 'none';
    document.getElementById('comment-input').value = '';
    currentMovieId = null;
}

async function submitComment() {
    if (!currentMovieId || !userId) {
        alert("Thiếu thông tin userId hoặc movieId!");
        return;
    }

    const content = document.getElementById('comment-input').value.trim();
    if (!content) {
        alert('Nhập nội dung bình luận!');
        return;
    }

    const body = {
        userId: parseInt(userId, 10),
        movieId: currentMovieId,
        content: content
    };

    const json = await fetchApi(`http://localhost:8080/user/add-comment`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (json.status && json.status.toLowerCase() === 'success') {
        document.getElementById('comment-input').value = '';
        showMovieDetail(await fetchMovieById(currentMovieId));
    } else {
        alert('Gửi comment thất bại!');
    }
}

// Event listeners
document.addEventListener("DOMContentLoaded", () => {
    const likeBtn = document.getElementById("like-btn");
    const saveBtn = document.getElementById("save-btn");

    if (likeBtn) {
        likeBtn.addEventListener("click", () => {
            if (currentMovieId) toggleInteraction(currentMovieId, userId, "LIKE");
        });
    }

    if (saveBtn) {
        saveBtn.addEventListener("click", () => {
            if (currentMovieId) toggleInteraction(currentMovieId, userId, "SAVE");
        });
    }

    // Load initial data
    loadWatchlist();
});