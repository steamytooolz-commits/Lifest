document.addEventListener("DOMContentLoaded", () => {
    // Navigation Tabs
    const navButtons = document.querySelectorAll(".nav-btn");
    const sections = document.querySelectorAll(".tab-section");
    const pageTitle = document.getElementById("page-title");

    navButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-tab");
            navButtons.forEach(b => b.classList.remove("active"));
            sections.forEach(s => s.classList.remove("active"));

            btn.classList.add("active");
            document.getElementById(target).classList.add("active");
            pageTitle.innerText = btn.innerText;
        });
    });

    // Handle Mint Coupon Form
    const couponForm = document.getElementById("coupon-form");
    if (couponForm) {
        couponForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const code = document.getElementById("coupon-code").value.trim().toUpperCase();
            const discount = document.getElementById("coupon-discount").value;
            const uses = document.getElementById("coupon-uses").value;
            const days = document.getElementById("coupon-days").value;

            const tbody = document.getElementById("coupon-table-body");
            const row = document.createElement("tr");
            row.innerHTML = `
                <td><code>${code}</code></td>
                <td>${discount}%</td>
                <td>0 / ${uses}</td>
                <td>${days} Days</td>
                <td><button class="revoke-btn" onclick="this.closest('tr').remove()">Revoke</button></td>
            `;
            tbody.prepend(row);
            alert(`Coupon ${code} created and signed with Ed25519.`);
            couponForm.reset();
        });
    }
});

// Admin Subscription Approval Functions
window.approveSub = function(rowId) {
    const row = document.getElementById(rowId);
    if (!row) return;
    const statusCell = row.querySelector("td:nth-child(5)");
    const actionCell = row.querySelector("td:nth-child(6)");
    if (statusCell) {
        statusCell.innerHTML = '<span class="badge" style="background:#00e676;color:#0b0f19;">APPROVED</span>';
    }
    if (actionCell) {
        actionCell.innerHTML = '<span style="color:#00e676;font-size:12px;font-weight:bold;">✓ Entitlement Issued</span>';
    }
    alert("Pass approved! Cryptographic entitlement token issued to citizen.");
};

window.rejectSub = function(rowId) {
    const row = document.getElementById(rowId);
    if (!row) return;
    const statusCell = row.querySelector("td:nth-child(5)");
    const actionCell = row.querySelector("td:nth-child(6)");
    if (statusCell) {
        statusCell.innerHTML = '<span class="badge" style="background:#ff5252;color:#ffffff;">REJECTED</span>';
    }
    if (actionCell) {
        actionCell.innerHTML = '<span style="color:#ff5252;font-size:12px;">✕ Request Denied</span>';
    }
};
