document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("image-modal");
    const modalImg = document.getElementById("image-modal-img");

    if (!modal || !modalImg) return;

    document.querySelectorAll(".clickable-image").forEach(function (img) {
        img.addEventListener("click", function () {
            if (!img.src) return;

            modalImg.src = img.src;
            modal.classList.add("is-open");
        });
    });

    modal.addEventListener("click", function () {
        modal.classList.remove("is-open");
        modalImg.removeAttribute("src");
    });
});