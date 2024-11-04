package com.company.mscategory.controller;

import com.company.mscategory.model.request.CategoryRequest;
import com.company.mscategory.model.request.CategoryUpdateRequest;
import com.company.mscategory.model.response.CategoryTreeNodeResponse;
import com.company.mscategory.service.abstracts.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryTreeNodeResponse> getCategories() {
        return categoryService.getCategories();
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public void createCategory(@RequestBody CategoryRequest categoryRequest) {
        categoryService.createCategory(categoryRequest);
    }

    @PutMapping("/{categoryId}")
    @ResponseStatus(NO_CONTENT)
    public void updateCategory(@PathVariable Long categoryId, @RequestBody CategoryUpdateRequest categoryUpdateRequest) {
        categoryService.updateCategory(categoryId, categoryUpdateRequest);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }

}

