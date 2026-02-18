(function () {
  function initSortable(root) {
    const list = root.querySelector("[data-sort-list]");
    const input = root.querySelector("input[name='orderedIds']");
    if (!list || !input) return;

    let dragging = null;
    let allowDrag = false; // ★ハンドル起点かどうか

    function updateHidden() {
      const keys = Array.from(list.querySelectorAll("[data-sort-item]"))
        .map(li => li.getAttribute("data-key"))
        .filter(Boolean);
      input.value = keys.join(",");
    }

    // ★ハンドルを押した時だけ allowDrag=true にする
    list.addEventListener("mousedown", (e) => {
      allowDrag = !!e.target.closest(".sort-handle");
    });
    list.addEventListener("touchstart", (e) => {
      allowDrag = !!e.target.closest(".sort-handle");
    }, { passive: true });

    // mouseup で解除
    document.addEventListener("mouseup", () => (allowDrag = false));
    document.addEventListener("touchend", () => (allowDrag = false));

    list.querySelectorAll("[data-sort-item]").forEach(li => {
      li.setAttribute("draggable", "true");

      li.addEventListener("dragstart", (e) => {
        // ★ハンドル起点以外は禁止
        if (!allowDrag) {
          e.preventDefault();
          return;
        }

        dragging = li;
        li.classList.add("dragging");
        e.dataTransfer.effectAllowed = "move";

        // Firefox 対策
        try {
          e.dataTransfer.setData("text/plain", li.getAttribute("data-key") || "");
        } catch (_) {}
      });

      li.addEventListener("dragend", () => {
        if (dragging) dragging.classList.remove("dragging");
        list.querySelectorAll(".over").forEach(x => x.classList.remove("over"));
        dragging = null;
        allowDrag = false;
        updateHidden();
      });

      li.addEventListener("dragover", (e) => {
        e.preventDefault();
        if (!dragging || li === dragging) return;

        li.classList.add("over");

        const rect = li.getBoundingClientRect();
        const isAfter = (e.clientY - rect.top) > rect.height / 2;

        if (isAfter) li.after(dragging);
        else li.before(dragging);
      });

      li.addEventListener("dragleave", () => li.classList.remove("over"));

      li.addEventListener("drop", (e) => {
        e.preventDefault();
        li.classList.remove("over");
        updateHidden();
      });
    });

    updateHidden();

    const form = root.querySelector("form");
    if (form) form.addEventListener("submit", updateHidden);
  }

  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-sort-root]").forEach(initSortable);
  });
})();
