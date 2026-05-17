package com.restaurant.enums;

public enum TableType {
    SMALL(2, "小桌"),
    MEDIUM(4, "中桌"),
    LARGE(6, "大桌"),
    EXTRA_LARGE(10, "超大桌");

    private final int capacity;
    private final String description;

    TableType(int capacity, String description) {
        this.capacity = capacity;
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getDescription() {
        return description;
    }

    public static TableType getByPeopleCount(int peopleCount) {
        if (peopleCount <= 2) return SMALL;
        if (peopleCount <= 4) return MEDIUM;
        if (peopleCount <= 6) return LARGE;
        return EXTRA_LARGE;
    }
}
