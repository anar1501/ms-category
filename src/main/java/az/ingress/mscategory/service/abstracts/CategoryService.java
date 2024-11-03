package az.ingress.mscategory.service.abstracts;

import az.ingress.mscategory.model.request.CategoryRequest;
import az.ingress.mscategory.model.request.CategoryUpdateRequest;
import az.ingress.mscategory.model.response.CategoryTreeNodeResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryTreeNodeResponse> getCategories();

    void createCategory(CategoryRequest categoryRequest);

    void updateCategory(Long categoryId, CategoryUpdateRequest categoryUpdateRequest);

    void deleteCategory(Long categoryId);
}
