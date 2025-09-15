document.addEventListener("DOMContentLoaded", function () {
    const logoutBtn = document.getElementById("logoutBtn");

    if (!logoutBtn) return;

    logoutBtn.addEventListener("click", function (event) {
        event.preventDefault();
        const message = "ログアウトしてもよろしいですか？";

        if (confirm(message)) {
            fetch("/logout", {
                method: "POST",
                headers: {
                    "X-CSRF-TOKEN": document.querySelector("meta[name='_csrf']").content,
                },
            }).then(() => {
                window.location.href = "/";
            });
        }
    });
});
