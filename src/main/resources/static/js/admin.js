document.addEventListener("DOMContentLoaded", () => {

    const addAdminForm = document.getElementById("addAdminForm");
    const adminFormMessage = document.getElementById("adminFormMessage");
    const loadUsersBtn = document.getElementById("loadUsersBtn");
    const loadAdminsBtn = document.getElementById("loadAdminsBtn");
    const usersTableBody = document.querySelector("#usersTable tbody");
    const adminsTableBody = document.querySelector("#adminsTable tbody");

    addAdminForm.addEventListener("submit", addAdmin);
    loadUsersBtn.addEventListener("click", loadUsers);
    loadAdminsBtn.addEventListener("click", loadAdmins);

    // Tải danh sách khi mở trang
    loadUsers();
    loadAdmins();

    // --- HÀM TẢI DANH SÁCH USERS (GET /admin/users) ---
    async function loadUsers() {
        try {
            const response = await fetch("http://localhost:8080/admin/users");
            const result = await response.json();

            usersTableBody.innerHTML = ""; // xóa bảng cũ

            if (result.status === "Success" && result.data.data) {
                result.data.data.forEach(user => {
                    const row = `
                        <tr>
                            <td>${user.userId}</td>
                            <td>${user.username}</td>
                            <td>${user.email}</td>
                            <td>${user.state}</td>
                            <td>
                                <button class="btn btn-lock" onclick="lockUser(${user.userId})">Khoá</button>
                                <button class="btn btn-unlock" onclick="unlockUser(${user.userId})">Mở Khoá</button>
                                <button class="btn btn-promote" onclick="promoteUser(${user.userId})">Nâng cấp</button>
                            </td>
                        </tr>
                    `;
                    usersTableBody.innerHTML += row;
                });
            }
        } catch (error) {
            console.log("Lỗi tải User:",error);
            usersTableBody.innerHTML = `<tr><td colspan="5">Không thể tải danh sách user.</td></tr>`;
        }
    }

    //Ds admin
    async function loadAdmins() {
        try {
            const response = await fetch("http://localhost:8080/admin/admins");
            const result = await response.json();

            adminsTableBody.innerHTML = "";
            if (result.status === "Success" && result.data.data) {
                result.data.forEach(admin => {
                    const row = `
                           <tr>
                            <td>${admin.userId}</td>
                            <td>${admin.username}</td>
                            <td>${admin.email}</td>
                            <td>
                                <button class="btn btn-lock" onclick="demoteUser(${admin.userId})">Giáng cấp</button>
                            </td>
                        </tr>
                    `;
                    adminsTableBody.innerHTML += row;
                })
            }
        } catch (error) {
            console.log(error);
            adminsTableBody.innerHTML = `<tr><td colspan="4">Không thể tải danh sách admin.</td></tr>`;
        }
    }

    // add admin
    async function addAdmin(e) {
        e.preventDefault();

        const username = document.getElementById("username").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        try {
            const response = await fetch("http://localhost:8080/admin/newad", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({username, email, password}),
            });
            const result = await response.json();

            showMessage(adminFormMessage, result.message, result.status === "Success" ? "success" : "error");

            adminsTableBody.innerHTML = "";
            if (result.status === "Success") {
                addAdminForm.reset();
                await loadAdmins();
            }
        }catch(error) {
            console.log("Lỗi thêm admin", error);
            showMessage(adminFormMessage, "Lỗi kết nối server.", "error");
        }
    }

    // API: PUT /admin/users/lock/{id}
    window.lockUser = async (userId) => {
        if (!confirm("Bạn có muốn khóa user này?")) return;
        await fetch(`http://localhost:8080/admin/users/lock/${userId}`, { method: "PUT" });
        await loadUsers();
    }

    // API: PUT /admin/users/unlock/{id}
    window.unlockUser = async (userId) => {
        if (!confirm("Bạn có muốn mở khóa user này?")) return;
        await fetch(`http://localhost:8080/admin/users/unlock/${userId}`, { method: "PUT" });
        await loadUsers();
    }

    // API: PUT /admin/users/promote/{id}
    window.promoteUser = async (userId) => {
        if (!confirm("Bạn có chắc muốn cho user này thành admin?")) return;
        await fetch(`http://localhost:8080/admin/users/promote/${userId}`, { method: "PUT" });
        await loadUsers();
        await loadAdmins();
    }

    // API: PUT /admin/users/demote/{id}
    window.demoteUser = async (userId) => {
        if (!confirm("Bạn có chắc muốn cho admin này thành user?")) return;
        await fetch(`http://localhost:8080/admin/users/demote/${userId}`, { method: "PUT" });
        await loadUsers();
        await loadAdmins();
    }

    function showMessage(element, text, type) {
        element.textContent = text;
        element.className = `message ${type}`;

    }
})