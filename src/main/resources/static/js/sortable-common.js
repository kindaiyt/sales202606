function initSortableList({ listId, hiddenInputId }) {
  const list = document.getElementById(listId);
  const hidden = document.getElementById(hiddenInputId);
  if (!list || !hidden) return;

  let dragging = null;

  const updateHidden = () => {
    const ids = [...list.querySelectorAll(".sort-item")]
      .map(li => li.getAttribute("data-id"))
      .filter(Boolean);
    hidden.value = ids.join(",");
  };

  updateHidden();

  list.querySelectorAll(".sort-item").forEach(item => {
    item.setAttribute("draggable", "true");

    item.addEventListener("dragstart", e => {
      dragging = item;
      item.classList.add("dragging");
      e.dataTransfer.effectAllowed = "move";
    });

    item.addEventListener("dragend", () => {
      if (dragging) dragging.classList.remove("dragging");
      dragging = null;
      updateHidden();
    });

    item.addEventListener("dragover", e => {
      e.preventDefault();
      const target = e.currentTarget;
      if (!dragging || target === dragging) return;

      const rect = target.getBoundingClientRect();
      const after = (e.clientY - rect.top) > rect.height / 2;

      if (after) target.after(dragging);
      else target.before(dragging);
    });
  });
}
