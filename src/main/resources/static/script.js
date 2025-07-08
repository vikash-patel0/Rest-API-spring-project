const API_URL = "http://localhost:8080/api/users";
const userForm = document.getElementById("userForm");
const adminTableBody = document.getElementById("adminTableBody");
const userTableBody = document.getElementById("userTableBody");
const messageBox = document.getElementById("messageBox");

// Helper: Format ISO datetime to 'YYYY-MM-DD HH:mm:ss'
function formatDateTime(dt) {
    if (!dt) return '';
    return dt.replace('T', ' ').substring(0, 19);
}

// Show a message to the user
function showMessage(msg, isError = false) {
    messageBox.textContent = msg;
    messageBox.style.background = isError
        ? "linear-gradient(90deg, #e74c3c 0%, #fdcbcb 100%)"
        : "linear-gradient(90deg, #38ef7d 0%, #11998e 100%)";
    messageBox.style.color = "#fff";
    messageBox.style.display = "block";
    setTimeout(() => {
        messageBox.textContent = "";
        messageBox.style.background = "none";
    }, 2500);
}

// Clear the form fields and error messages
function clearForm() {
    userForm.reset();
    document.getElementById("userId").value = "";
    document.getElementById("nameError").textContent = "";
    document.getElementById("emailError").textContent = "";
    document.getElementById("passwordError").textContent = "";
    document.getElementById("rolesError").textContent = "";
}


// Render users in the tables
function renderUsers(users) {
    adminTableBody.innerHTML = "";
    userTableBody.innerHTML = "";

    const admins = users.filter(user => user.roles && user.roles.includes("ROLE_ADMIN"));
    const normalUsers = users.filter(user => !user.roles || !user.roles.includes("ROLE_ADMIN"));

    // Render Admins
    if (admins.length === 0) {
        adminTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#888;">No admins found.</td></tr>`;
    } else {
        admins.forEach(user => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td>${user.createdAt ? formatDateTime(user.createdAt) : '-'}</td>
                <td>${user.updatedAt ? formatDateTime(user.updatedAt) : '-'}</td>
                <td class="actions">
                    <button class="edit" data-id="${user.id}">Edit</button>
                    <button class="delete" data-id="${user.id}">Delete</button>
                </td>
            `;
            adminTableBody.appendChild(tr);
        });
    }

    // Render Users
    if (normalUsers.length === 0) {
        userTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#888;">No users found.</td></tr>`;
    } else {
        normalUsers.forEach(user => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td>${user.createdAt ? formatDateTime(user.createdAt) : '-'}</td>
                <td>${user.updatedAt ? formatDateTime(user.updatedAt) : '-'}</td>
                <td class="actions">
                    <button class="edit" data-id="${user.id}">Edit</button>
                    <button class="delete" data-id="${user.id}">Delete</button>
                </td>
            `;
            userTableBody.appendChild(tr);
        });
    }
}

// Fetch all users (GET)
async function fetchUsers() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error();
        const users = await response.json();
        renderUsers(users);
    } catch (err) {
        adminTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#e74c3c;">Failed to load admins.</td></tr>`;
        userTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#e74c3c;">Failed to load users.</td></tr>`;
        showMessage("Failed to load users.", true);
    }
}

// Add or update user (POST or PUT)
async function saveUser(event) {
    event.preventDefault();
    const id = document.getElementById("userId").value;
    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const rolesInput = document.getElementById("roles").value.trim();

    // Basic validation
    let valid = true;
    document.getElementById("nameError").textContent = "";
    document.getElementById("emailError").textContent = "";
    document.getElementById("passwordError").textContent = "";
    if (!name) {
        document.getElementById("nameError").textContent = "Name is required.";
        valid = false;
    }
    if (!email) {
        document.getElementById("emailError").textContent = "Email is required.";
        valid = false;
    }
    if (!password && !id) { // Require password only when adding new user
        document.getElementById("passwordError").textContent = "Password is required.";
        valid = false;
    }
    if (!valid) return;

    // Build user object
    const user = { name, email };
    if (password) user.password = password;
    if (rolesInput) {
        user.roles = rolesInput.split(",").map(role => role.trim());
    }

    let url = API_URL;
    let method = "POST";
    let body = JSON.stringify(user);

    if (id) {
        url += `/${id}`;
        method = "PUT";
        body = JSON.stringify(user);
    }

    try {
        const response = await fetch(url, {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: body
        });
        if (response.ok) {
            showMessage(id ? "User updated successfully!" : "User added successfully!");
            clearForm();
            fetchUsers();
        } else {
            let errMsg = "Save failed.";
            try {
                const errJson = await response.json();
                if (errJson && errJson.message) errMsg = errJson.message;
            } catch {}
            showMessage(errMsg, true);
        }
    } catch {
        showMessage("Save failed.", true);
    }
}


// Edit user (GET by id)
async function editUser(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error();
        const user = await response.json();
        document.getElementById("userId").value = user.id;
        document.getElementById("name").value = user.name;
        document.getElementById("email").value = user.email;
        showMessage("Loaded user for editing.");
    } catch {
        showMessage("Failed to load user.", true);
    }
}

// Delete user (DELETE)
async function deleteUser(id) {
    if (!confirm("Are you sure you want to delete this user?")) return;
    try {
        const response = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
        if (response.status === 204) {
            showMessage("User deleted.");
            fetchUsers();
        } else {
            showMessage("Delete failed.", true);
        }
    } catch {
        showMessage("Delete failed.", true);
    }
}

// Event delegation for edit and delete buttons for both tables
function handleTableClick(e) {
    if (e.target.classList.contains("edit")) {
        const id = e.target.getAttribute("data-id");
        editUser(id);
    }
    if (e.target.classList.contains("delete")) {
        const id = e.target.getAttribute("data-id");
        deleteUser(id);
    }
}
adminTableBody.addEventListener("click", handleTableClick);
userTableBody.addEventListener("click", handleTableClick);

// Form submission handler
userForm.addEventListener("submit", saveUser);

// Initial load
fetchUsers();
