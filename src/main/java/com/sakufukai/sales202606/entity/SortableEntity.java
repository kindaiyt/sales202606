package com.sakufukai.sales202606.entity;

public interface SortableEntity {
    String getSortKey();
    Integer getSortOrder();
    void setSortOrder(Integer order);
}
