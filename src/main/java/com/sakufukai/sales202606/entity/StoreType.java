package com.sakufukai.sales202606.entity;

public enum StoreType {
    STUDENT("学生店舗"), // 学生の模擬店舗
    GENERAL("一般店舗"); // 一般店舗

    private final String displayName;

    StoreType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}