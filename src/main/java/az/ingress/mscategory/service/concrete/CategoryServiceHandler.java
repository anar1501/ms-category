package az.ingress.mscategory.service.concrete;

import az.ingress.mscategory.aspect.Log;
import az.ingress.mscategory.dao.entity.CategoryEntity;
import az.ingress.mscategory.dao.repository.CategoryRepository;
import az.ingress.mscategory.exception.CannotDeleteSubCategoryException;
import az.ingress.mscategory.exception.NotFoundException;
import az.ingress.mscategory.model.enums.CategoryStatus;
import az.ingress.mscategory.model.request.CategoryRequest;
import az.ingress.mscategory.model.request.CategoryUpdateRequest;
import az.ingress.mscategory.model.response.CategoryTreeNodeResponse;
import az.ingress.mscategory.service.abstracts.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static az.ingress.mscategory.exception.ExceptionConstraints.CANNOT_DELETE_SUBCATEGORY_CODE;
import static az.ingress.mscategory.exception.ExceptionConstraints.CANNOT_DELETE_SUBCATEGORY_MESSAGE;
import static az.ingress.mscategory.exception.ExceptionConstraints.CATEGORY_NOT_FOUND_CODE;
import static az.ingress.mscategory.exception.ExceptionConstraints.CATEGORY_NOT_FOUND_MESSAGE;
import static az.ingress.mscategory.mapper.factory.CategoryMapper.CATEGORY_MAPPER;

@Log
@Service
@RequiredArgsConstructor
public class CategoryServiceHandler implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CacheServiceHandler cacheServiceHandler;

    @Override
    public List<CategoryTreeNodeResponse> getCategories() {
        var categoriesFromCache = cacheServiceHandler.getCategoriesFromCache();
        if (categoriesFromCache == null) {
            try {
                categoriesFromCache = cacheServiceHandler.saveCategoriesToCache().get();
            } catch (InterruptedException | ExecutionException e) {
                return List.of();
            }
        }
        return CATEGORY_MAPPER.buildCategoryTree(categoriesFromCache);
    }

    @Transactional
    @Override
    public void createCategory(CategoryRequest categoryRequest) {
        var separationResult = CATEGORY_MAPPER.separateCategories(categoryRequest.getCategories());
        var allCategories = new ArrayList<CategoryEntity>();
        allCategories.addAll(CATEGORY_MAPPER.mapBaseCategoryDetailToListCategoryEntity(separationResult.getBaseCategories()));
        allCategories.addAll(CATEGORY_MAPPER.mapSubCategoryDetailToListCategoryEntity(separationResult.getSubCategories()));
        categoryRepository.saveAll(allCategories);
        cacheServiceHandler.saveCategoriesToCache();
    }

    @Transactional
    @Override
    public void updateCategory(Long categoryId, CategoryUpdateRequest categoryUpdateRequest) {
        var categoryEntity = fetchCategoryEntityIfExist(categoryId);
        categoryEntity.setName(categoryUpdateRequest.getName());
        categoryEntity.setPicture(categoryUpdateRequest.getPicture());
        if (categoryUpdateRequest.getBaseId() != null) {
            fetchCategoryEntityIfExist(categoryUpdateRequest.getBaseId());
            categoryEntity.setBaseId(categoryUpdateRequest.getBaseId());
        } else {
            categoryEntity.setBaseId(null);
        }
        categoryRepository.save(categoryEntity);
        cacheServiceHandler.saveCategoriesToCache();
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        var categoryEntity = fetchCategoryEntityIfExist(categoryId);
        var entitiesToUpdate = new ArrayList<CategoryEntity>();
        if (categoryEntity.getBaseId() == null) {
            var subCategories = categoryRepository.findByBaseId(categoryId);
            subCategories.forEach(subCategory -> {
                subCategory.setStatus(CategoryStatus.DELETED);
                entitiesToUpdate.add(subCategory);
            });
        } else {
            throw new CannotDeleteSubCategoryException(CANNOT_DELETE_SUBCATEGORY_MESSAGE + categoryId, CANNOT_DELETE_SUBCATEGORY_CODE);
        }
        categoryEntity.setStatus(CategoryStatus.DELETED);
        entitiesToUpdate.add(categoryEntity);
        categoryRepository.saveAll(entitiesToUpdate);
        cacheServiceHandler.saveCategoriesToCache();
    }

    private CategoryEntity fetchCategoryEntityIfExist(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException(
                CATEGORY_NOT_FOUND_MESSAGE, CATEGORY_NOT_FOUND_CODE));
    }
}

