package com.product.api.service;

import java.util.List;

import com.product.api.entity.Category;
import com.product.api.dto.ApiResponse;


public interface SvcCategory {
    List<Category> getCategories() throws Exception;
    Category getCategory(Integer category_id);
    ApiResponse createCategory(Category category);
    ApiResponse updateCategory(Integer category_id, Category category);
    ApiResponse deleteCategory(Integer category_id);
}
