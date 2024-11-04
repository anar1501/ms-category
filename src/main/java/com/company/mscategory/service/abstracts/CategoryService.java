package com.company.mscategory.service.abstracts;

import com.company.mscategory.model.request.CategoryRequest;
import com.company.mscategory.model.request.CategoryUpdateRequest;
import com.company.mscategory.model.response.CategoryTreeNodeResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryTreeNodeResponse> getCategories();

    void createCategory(CategoryRequest categoryRequest);

    void updateCategory(Long categoryId, CategoryUpdateRequest categoryUpdateRequest);

    void deleteCategory(Long categoryId);
}
