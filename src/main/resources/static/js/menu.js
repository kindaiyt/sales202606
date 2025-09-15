document.addEventListener("DOMContentLoaded", function () {
    const hamburger = document.getElementById("hamburger");
    const menu = document.getElementById("menu");

    if (!hamburger || !menu) return;

    // メニューの閉じる高さ（0に戻す）
    const closeMenu = () => {
        menu.style.height = "0";
        menu.style.padding = "0 0";
        menu.classList.remove("active");
    };

    hamburger.addEventListener("click", (event) => {
        event.stopPropagation();

        if (!menu.classList.contains("active")) {
            menu.style.height = menu.scrollHeight + "px"; // 中身の高さに合わせる
            menu.style.padding = "10px";
            menu.classList.add("active");
        } else {
            closeMenu();
        }
    });

    menu.addEventListener("click", (event) => {
        event.stopPropagation();
    });

    document.addEventListener("click", () => {
        if (menu.classList.contains("active")) {
            closeMenu();
        }
    });
});
