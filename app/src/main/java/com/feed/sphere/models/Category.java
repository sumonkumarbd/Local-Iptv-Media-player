package com.feed.sphere.models;

public class Category {
    private final String categoryId;
    private final String categoryName;
    private final String parentId;

    public Category(String categoryId, String categoryName, String parentId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentId = parentId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}