package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    String createCategory(Category category);
    String deletCategory(Long categoryId);
    Category updateCategory(Category category,Long categoryId);
}
