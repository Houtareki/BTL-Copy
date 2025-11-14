document.addEventListener("DOMContentLoaded", () => {
    const addAdminForm = document.getElementById("addAdminForm");
    const adminFormMessage = document.getElementById("adminFormMessage");
    const loadUsersBtn = document.getElementById("loadUsersBtn");
    const loadAdminsBtn = document.getElementById("loadAdminsBtn");
    const usersTableBody = document.querySelector("#usersTable tbody");
    const adminsTableBody = document.querySelector("#adminsTable tbody");

    // Event listeners
    addAdminForm.addEventListener("submit", addAdmin);
    loadUsersBtn.addEventListener("click", loadUsers);
    loadAdminsBtn.addEventListener("click", loadAdmins);

    // Tải danh sách khi mở trang
    loadUsers();
    loadAdmins();

    // --- LOAD USERS ---
    async function loadUsers() {
        try {
            const response = await fetch("http://localhost:8080/admin/users", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Users result:", result);

            usersTableBody.innerHTML = "";

            if (result.status === "Success" && result.data && result.data.data) {
                const users = result.data.data;

                if (users.length === 0) {
                    usersTableBody.innerHTML = `<tr><td colspan="5">Không có user nào.</td></tr>`;
                    return;
                }

                users.forEach(user => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${user.userId}</td>
                        <td>${user.username}</td>
                        <td>${user.email}</td>
                        <td>${user.state}</td>
                        <td>
                            <button class="btn btn-lock" onclick="lockUser(${user.userId})">Khóa</button>
                            <button class="btn btn-unlock" onclick="unlockUser(${user.userId})">Mở Khóa</button>
                            <button class="btn btn-promote" onclick="promoteUser(${user.userId})">Nâng cấp</button>
                        </td>
                    `;
                    usersTableBody.appendChild(row);
                });
            } else {
                usersTableBody.innerHTML = `<tr><td colspan="5">Không thể tải danh sách user.</td></tr>`;
            }
        } catch (error) {
            console.error("Lỗi tải User:", error);
            usersTableBody.innerHTML = `<tr><td colspan="5">Lỗi kết nối: ${error.message}</td></tr>`;
        }
    }

    // --- LOAD ADMINS ---
    async function loadAdmins() {
        try {
            const response = await fetch("http://localhost:8080/admin/admins", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Admins result:", result);

            adminsTableBody.innerHTML = "";

            if (result.status === "Success" && result.data && result.data.data) {
                const admins = result.data.data;

                if (admins.length === 0) {
                    adminsTableBody.innerHTML = `<tr><td colspan="4">Không có admin nào.</td></tr>`;
                    return;
                }

                admins.forEach(admin => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${admin.userId}</td>
                        <td>${admin.username}</td>
                        <td>${admin.email}</td>
                        <td>
                            <button class="btn btn-lock" onclick="demoteUser(${admin.userId})">Giáng cấp</button>
                        </td>
                    `;
                    adminsTableBody.appendChild(row);
                });
            } else {
                adminsTableBody.innerHTML = `<tr><td colspan="4">Không thể tải danh sách admin.</td></tr>`;
            }
        } catch (error) {
            console.error("Lỗi tải Admin:", error);
            adminsTableBody.innerHTML = `<tr><td colspan="4">Lỗi kết nối: ${error.message}</td></tr>`;
        }
    }

    // --- ADD ADMIN ---
    async function addAdmin(e) {
        e.preventDefault();

        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!username || !email || !password) {
            showMessage(adminFormMessage, "Vui lòng điền đầy đủ thông tin", "error");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/admin/newad", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username, email, password }),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Add admin result:", result);

            showMessage(adminFormMessage, result.message, result.status === "Success" ? "success" : "error");

            if (result.status === "Success") {
                addAdminForm.reset();
                await loadAdmins();
                await loadUsers();
            }
        } catch (error) {
            console.error("Lỗi thêm admin:", error);
            showMessage(adminFormMessage, "Lỗi kết nối server: " + error.message, "error");
        }
    }

    // --- LOCK USER ---
    window.lockUser = async (userId) => {
        if (!confirm("Bạn có muốn khóa user này?")) return;

        try {
            const response = await fetch(`http://localhost:8080/admin/users/lock/${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Lock result:", result);

            alert(result.message || "Đã khóa user");
            await loadUsers();
        } catch (error) {
            console.error("Lỗi khóa user:", error);
            alert("Lỗi kết nối: " + error.message);
        }
    }

    // --- UNLOCK USER ---
    window.unlockUser = async (userId) => {
        if (!confirm("Bạn có muốn mở khóa user này?")) return;

        try {
            const response = await fetch(`http://localhost:8080/admin/users/unlock/${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Unlock result:", result);

            alert(result.message || "Đã mở khóa user");
            await loadUsers();
        } catch (error) {
            console.error("Lỗi mở khóa user:", error);
            alert("Lỗi kết nối: " + error.message);
        }
    }

    // --- PROMOTE USER ---
    window.promoteUser = async (userId) => {
        if (!confirm("Bạn có chắc muốn cho user này thành admin?")) return;

        try {
            const response = await fetch(`http://localhost:8080/admin/users/promote/${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Promote result:", result);

            alert(result.message || "Đã nâng cấp thành admin");
            await loadUsers();
            await loadAdmins();
        } catch (error) {
            console.error("Lỗi nâng cấp user:", error);
            alert("Lỗi kết nối: " + error.message);
        }
    }

    // --- DEMOTE USER ---
    window.demoteUser = async (userId) => {
        if (!confirm("Bạn có chắc muốn cho admin này thành user?")) return;

        try {
            const response = await fetch(`http://localhost:8080/admin/users/demote/${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log("Demote result:", result);

            alert(result.message || "Đã giáng cấp thành user");
            await loadUsers();
            await loadAdmins();
        } catch (error) {
            console.error("Lỗi giáng cấp admin:", error);
            alert("Lỗi kết nối: " + error.message);
        }
    }

    // --- SHOW MESSAGE ---
    function showMessage(element, text, type) {
        element.textContent = text;
        element.className = `message ${type}`;

        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            element.textContent = "";
            element.className = "message";
        }, 5000);
    }
});