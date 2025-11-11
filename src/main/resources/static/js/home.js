
let currentMovieId = null;
// Lấy userId từ query string
const params = new URLSearchParams(window.location.search);
const userId = params.get("userId"); // VD: ?userId=5  -> userId = "5"

const userPageLink = document.getElementById('account-link');
userPageLink.href = `http://localhost:8080/user/personal?userId=${parseInt(userId)}`;

function scrollRow(rowId, direction) {
    const row = document.getElementById(rowId);
    if (!row) return;

    const card = row.querySelector(".movie-card");
    if (!card) return;

    const cardWidth = card.offsetWidth + 12; // width + gap
    const visibleCards = Math.floor(row.offsetWidth / cardWidth);
    const maxScroll = row.scrollWidth - row.offsetWidth;

    let newScroll = row.scrollLeft + direction * cardWidth;

    if (newScroll < 0) {
        if (newScroll > -cardWidth) {
            // đang ở đầu mà bấm sang trái nhưng vẫn còn 1 card ẩn → nhảy về đầu
            newScroll = 0;
        } else {
            // đang ở đầu mà bấm sang trái và không còn card ẩn → nhảy về cuối
            newScroll = maxScroll;
        }
    } else if (newScroll >= maxScroll) {
        if (newScroll + 1 < maxScroll + cardWidth) {
            // đang ở cuối mà bấm sang phải nhưng vẫn còn 1 card ẩn → nhảy về cuối
            newScroll = maxScroll;
        } else {
            // đang ở cuối mà bấm sang phải và không còn card ẩn nữa    
            newScroll = 0;
        }
    }

    row.scrollTo({
        left: newScroll,
        behavior: "smooth"
    });
}


async function fetchApi(url, options = {}) {
    try {
        const res = await fetch(url, options);
        const json = await res.json();
        if (!json || typeof json.status !== 'string') return { status: 'Error', data: { data: [] } };
        return json;
    } catch (err) { console.error('Lỗi fetchApi:', err); return { status: 'Error', data: { data: [] } }; }
}

async function fetchMovieById(movieId) { const json = await fetchApi(`http://localhost:8080/user/get-movie?movieId=${movieId}`); return json.data?.data || null; }
async function fetchEpisodes(movieId) { const json = await fetchApi(`http://localhost:8080/user/get-episodes?movieId=${movieId}`); return json; }
async function fetchComments(movieId) { const json = await fetchApi(`http://localhost:8080/user/get-comments-by-movieId?userId=${userId}&movieId=${movieId}`); return json; }

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

    fetchEpisodes(movie.movieId).then(episodes => {
        //console.log(episodes);

        const container = document.getElementById('episode-list');
        container.innerHTML = '';

        if (episodes.status === 'Error' || !episodes.data || episodes.data.data.length === 0) {
            container.innerHTML = `
                    <div style="
                      color: #666;
                      font-style: italic;
                      font-family: Arial, sans-serif;
                      font-size: 14px;
                      text-align: center;
                      margin: 15px 0;
                    ">
                      Lỗi tải tập phim! Vui lòng tải lại trang!
                    </div>
                    `;
            return;
        }

        episodes.data.data.forEach(ep => {
            const div = document.createElement('div');
            div.className = 'episode-card';

            // Tạo link nhưng xử lý nhấn qua JS để chèn logic update view
            const a = document.createElement('a');
            a.href = ep.videoUrl;
            a.target = "_blank";
            a.textContent = ep.name;

            a.addEventListener('click', async (e) => {
                // Gọi API update lượt xem
                await fetchApi(`http://localhost:8080/user/interaction-watch?userId=${parseInt(userId)}&movieId=${currentMovieId}`, { method: 'PUT' });

                // Sau khi update thì gọi lại API lấy movie mới
                //const updatedMovie = await fetchMovieById(movie.movieId);
                const updatedMovie = await fetchInteraction(currentMovieId, parseInt(userId));
            });

            div.appendChild(a);
            container.appendChild(div);
        });
    });


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

function closeMovieDetail() { document.getElementById('movie-detail').style.display = 'none'; document.getElementById('comment-input').value = ''; currentMovieId = null; }

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
        // load lại movie detail để refresh comment
        showMovieDetail(await fetchMovieById(currentMovieId));
    } else {
        alert('Gửi comment thất bại!');
    }
}


// Banner & movie rows

async function renderBanner() {
    try {
        const res = await fetch(`http://localhost:8080/user/get-banner`);
        const json = await res.json();
        const movies = json.data?.data || [];

        const bannerEl = document.getElementById('banner');
        const bannerTitle = document.getElementById('banner-title');
        const bannerBtn = document.getElementById('banner-btn');
        const bannerTrack = document.querySelector('.banner-track');
        bannerTrack.innerHTML = movies
            .slice(0, 10)
            .map(movie => `<img src="${movie.thumbUrl}">`)
            .join('');

        let index = 0;
        const total = bannerTrack.children.length;
        function showMovie(index_) { const movie = movies[index_]; bannerTitle.innerText = movie.title; bannerBtn.onclick = () => showMovieDetail(movie); }
        showMovie(index);

        setInterval(() => {
            index = (index + 1) % total;
            bannerTrack.style.transform = `translateX(-${index * 100}%)`;
            showMovie(index);
        }, 5000);

    } catch (err) {
        console.error("Lỗi tải banner:", err);
    }
}


async function renderMovieRow(containerId, apiUrl) {
    const container = document.getElementById(containerId);
    const json = await fetchApi(apiUrl);
    //console.log(json);
    const movies = Array.isArray(json.data?.data) ? json.data.data : [json.data?.data];
    if (json.status === "Error") {
        container.previousElementSibling.style.display = 'none';
        container.innerHTML = '<p style="color:#aaa; font-style:italic;">Chưa có phim nào trong danh sách.</p>';
        container.nextElementSibling.style.display = 'none';
        return;
    }

    // Hiển thị tiêu đề và nút điều hướng
    container.previousElementSibling.style.display = 'block';
    container.nextElementSibling.style.display = 'block';

    container.innerHTML = '';

    movies.forEach(movie => {
        const card = document.createElement('div');
        card.className = 'movie-card';
        card.style.position = 'relative';

        card.innerHTML = `
          <img src="${movie.thumbUrl}" alt="${movie.title}" title="${movie.title}">
          <div class="overlay">
              <h4>${movie.title}</h4>
              <p>${movie.description || 'Chưa có mô tả'}</p>
          </div>
        `;
        card.onclick = () => showMovieDetail(movie);
        container.appendChild(card);
    });

    const cardWidth = container.firstChild.offsetWidth + 10;
    const numberOfCards = container.children.length;
    if (cardWidth * numberOfCards <= container.offsetWidth) {
        container.previousElementSibling.style.display = 'none';
        container.nextElementSibling.style.display = 'none';
    }
}

// Cập nhật renderGenres
async function renderGenres() {
    const container = document.getElementById('genres');
    const json = await fetchApi(`http://localhost:8080/user/get-genres?userId=${userId}`);
    let genres = Array.isArray(json.data?.data) ? json.data.data : [json.data?.data];
    if (!genres) genres = [];
    // Code html cho container chứa các card lưu các thể loại
    container.innerHTML = '';
    // Code html cho bảng hiển thị các thể loại theo dạng lưới trong navbar
    const cols = 4; // số cột của bảng
    let genreList = "<table><tr>";
    let i = 0;
    genres.forEach(g => {
        //Tạo card cho mỗi thể loại
        const card = document.createElement('div');
        card.className = 'genre-card';
        card.innerText = g.name;
        card.onclick = () => {
            window.location.href = `movies-of-genre?userId=${parseInt(userId)}&genre=${g.genreId}&genreName=${g.name.replaceAll(" ", "_")}`;
        };
        container.appendChild(card);
        // Tạo bảng
        genreList += `<td><a href="http://localhost:8080/user/movies-of-genre?userId=${parseInt(userId)}&genre=${g.genreId}&genreName=${g.name.replaceAll(" ", "_")}" target="_self">${g.name}</a></td>`;
        if ((i + 1) % cols === 0) genreList += "</tr><tr>";
        i++;
    });
    genreList += "</tr></table>";
    document.getElementById("genre-table").innerHTML = genreList;
}

// Cập nhật renderCountries
async function renderCountries() {
    const container = document.getElementById('countries');
    const json = await fetchApi(`http://localhost:8080/user/get-countries?userId=${userId}`);
    let countries = Array.isArray(json.data?.data) ? json.data.data : [json.data?.data];
    if (!countries) countries = [];
    // Code html cho container chứa các card lưu các country
    container.innerHTML = '';
    // Code html cho bảng hiển thị các country theo dạng lưới trong navbar
    const cols = 3; // số cột của bảng
    let countryList = "<table><tr>";
    let i = 0;
    countries.forEach(c => {
        //Tạo card cho mỗi quốc gia
        const card = document.createElement('div');
        card.className = 'country-card';
        card.innerText = c;
        card.onclick = () => {
            //str.replaceAll(" ", "_");
            window.location.href = `movies-of-country?userId=${parseInt(userId)}&country=${c.replaceAll(" ", "_")}`;
        };
        container.appendChild(card);
        // Tạo bảng
        countryList += `<td><a href="http://localhost:8080/user/movies-of-country?userId=${parseInt(userId)}&country=${c.replaceAll(" ", "_")}" target="_self">${c}</a></td>`;
        if ((i + 1) % cols === 0) countryList += "</tr><tr>";
        i++;
    });
    countryList += "</tr></table>";
    document.getElementById("country-table").innerHTML = countryList;
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
        }
        else if (action === "SAVE") {
            interactionUrl = `http://localhost:8080/user/interaction-save?userId=${userId}&movieId=${movieId}`;
        }

        const res = await fetch(interactionUrl, {
            method: "PUT",
        });
        const result = await res.json();
        if (result.status === "Success") {
            await fetchInteraction(movieId, userId);
            if (action === "LIKE") {
                await renderMovieRow('liked', `http://localhost:8080/user/get-liked-movies-by-userId?userId=${userId}`);
            }
            else if (action === "SAVE") {
                await renderMovieRow('saved', `http://localhost:8080/user/get-saved-movies-by-userId?userId=${userId}`);
            }
        }
    } catch (err) {
        console.error("toggleInteraction error:", err);
    }

}

// Gắn sự kiện click
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

});


async function renderHome() {
    await renderBanner();
    await renderMovieRow('showing-movies', `http://localhost:8080/user/get-showing-movies?userId=${userId}`, 'Xem Ngay');
    await renderMovieRow('recommended-movies', `http://localhost:8080/user/get-suggested-movies?userId=${userId}`, 'Xem Ngay');
    await renderGenres();
    await renderCountries();
}

document.getElementById("logo").addEventListener("click", function () {
    location.reload(); // reload lại trang hiện tại
});

renderHome();
