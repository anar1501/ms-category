package com.company.mscategory.service


import com.company.mscategory.dao.entity.CategoryEntity
import com.company.mscategory.dao.repository.CategoryRepository
import com.company.mscategory.exception.CannotDeleteSubCategoryException
import com.company.mscategory.exception.NotFoundException
import com.company.mscategory.model.enums.CategoryStatus
import com.company.mscategory.model.request.CategoryRequest
import com.company.mscategory.model.request.CategoryUpdateRequest
import com.company.mscategory.model.response.CategorySeparationResult
import com.company.mscategory.model.response.CategoryTreeNodeResponse
import com.company.mscategory.service.concrete.CacheServiceHandler
import com.company.mscategory.service.concrete.CategoryServiceHandler
import io.github.benas.randombeans.EnhancedRandomBuilder
import io.github.benas.randombeans.api.EnhancedRandom
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import static com.company.mscategory.mapper.factory.CategoryMapper.CATEGORY_MAPPER

class CategoryServiceTest extends Specification {
    EnhancedRandom random = EnhancedRandomBuilder.aNewEnhancedRandom()

    CategoryRepository categoryRepository
    CacheServiceHandler cacheServiceHandler
    CategoryServiceHandler categoryServiceHandler

    def setup() {
        categoryRepository = Mock()
        cacheServiceHandler = Mock()
        categoryServiceHandler = new CategoryServiceHandler(categoryRepository, cacheServiceHandler)
    }

    def "getCategories() method must return categories from cache if available"() {
        given: "categories are available in the cache"
        def cachedCategories = [new CategoryEntity(id: 1L, name: "Category 1", baseId: null)]
        cacheServiceHandler.getCategoriesFromCache() >> cachedCategories

        and: "CATEGORY_MAPPER returns a built category tree"
        def categoryTree = [new CategoryTreeNodeResponse(id: 1L, name: "Category 1")]
        CATEGORY_MAPPER.buildCategoryTree(cachedCategories) >> categoryTree

        when: "getCategories is called"
        def result = categoryServiceHandler.getCategories()

        then: "categories are returned directly from cache"
        result == categoryTree
        0 * cacheServiceHandler.saveCategoriesToCache()
    }

    def "getCategories() method should save categories to cache if cache is empty and return built category tree"() {
        given: "cache is empty and saveCategoriesToCache returns categories"
        cacheServiceHandler.getCategoriesFromCache() >> null

        and: "saveCategoriesToCache returns categories from the database"
        def dbCategories = [new CategoryEntity(id: 1L, name: "Category 1", baseId: null)]
        cacheServiceHandler.saveCategoriesToCache() >> CompletableFuture.completedFuture(dbCategories)

        and: "CATEGORY_MAPPER builds a category tree"
        def categoryTree = [new CategoryTreeNodeResponse(id: 1L, name: "Category 1")]
        CATEGORY_MAPPER.buildCategoryTree(dbCategories) >> categoryTree

        when: "getCategories is called"
        def result = categoryServiceHandler.getCategories()

        then: "categories are retrieved from the database, saved to cache, and returned as a tree"
        result == categoryTree
    }

    def "getCategories() method should return empty list if saveCategoriesToCache throws an exception"() {
        given: "cache is empty and saveCategoriesToCache throws an ExecutionException"
        cacheServiceHandler.getCategoriesFromCache() >> null
        cacheServiceHandler.saveCategoriesToCache() >> CompletableFuture.failedFuture(new ExecutionException("Error", new Throwable()))

        when: "getCategories is called"
        def result = categoryServiceHandler.getCategories()

        then: "an empty list is returned due to the exception"
        result == []
    }

    def "createCategory() method must save all categories and call cache service"() {
        given: "a CategoryRequest with base and sub-categories"
        def baseCategoryDetail = new CategoryRequest.CategoryDetail(name: "Base Category", baseId: null, picture: "base-pic.png")
        def subCategoryDetail = new CategoryRequest.CategoryDetail(name: "Sub Category", baseId: 1L, picture: "sub-pic.png")
        def categoryRequest = new CategoryRequest(categories: [baseCategoryDetail, subCategoryDetail])

        and: "separated categories returned by CATEGORY_MAPPER"
        def separationResult = new CategorySeparationResult(
                baseCategories: [baseCategoryDetail],
                subCategories: [subCategoryDetail]
        )
        CATEGORY_MAPPER.separateCategories(_) >> separationResult

        and: "mapped entities returned by CATEGORY_MAPPER"
        def baseCategoryEntity = new CategoryEntity(name: "Base Category", baseId: null, picture: "base-pic.png")
        def subCategoryEntity = new CategoryEntity(name: "Sub Category", baseId: 1L, picture: "sub-pic.png")
        CATEGORY_MAPPER.mapBaseCategoryDetailToListCategoryEntity(_) >> [baseCategoryEntity]
        CATEGORY_MAPPER.mapSubCategoryDetailToListCategoryEntity(_) >> [subCategoryEntity]

        when: "createCategory is called"
        categoryServiceHandler.createCategory(categoryRequest)

        then: "categoryRepository.saveAll is called with all mapped categories"
        1 * categoryRepository.saveAll([baseCategoryEntity, subCategoryEntity])

        and: "cacheServiceHandler.saveCategoriesToCache is called"
        1 * cacheServiceHandler.saveCategoriesToCache()
    }

    def "updateCategory() method must update category and call cache service"() {
        given: "an existing category entity and a CategoryUpdateRequest"
        def categoryId = random.nextObject(Long)
        def categoryUpdateRequest = new CategoryUpdateRequest(name: "Updated Category", baseId: 2L, picture: "updated-pic.png")
        def existingCategory = new CategoryEntity(id: categoryId, name: "Old Category", baseId: null, picture: "old-pic.png")
        def baseCategory = new CategoryEntity(id: 2L, name: "Base Category", baseId: null, picture: "base-pic.png")

        and: "fetchCategoryEntityIfExist returns the existing category and base category"
        categoryRepository.findById(categoryId) >> Optional.of(existingCategory)
        categoryRepository.findById(categoryUpdateRequest.baseId) >> Optional.of(baseCategory)

        when: "updateCategory is called"
        categoryServiceHandler.updateCategory(categoryId, categoryUpdateRequest)

        then: "the existing category entity is updated with new values"
        existingCategory.name == categoryUpdateRequest.name
        existingCategory.picture == categoryUpdateRequest.picture
        existingCategory.baseId == categoryUpdateRequest.baseId

        and: "categoryRepository.save is called with the updated category entity"
        1 * categoryRepository.save(existingCategory)

        and: "cacheServiceHandler.saveCategoriesToCache is called to update the cache"
        1 * cacheServiceHandler.saveCategoriesToCache()
    }

    def "updateCategory() method should throw NotFoundException if category does not exist"() {
        given: "a non-existent category ID and a CategoryUpdateRequest"
        def nonExistentCategoryId = random.nextObject(Long)
        def categoryUpdateRequest = new CategoryUpdateRequest(name: "Updated Category", baseId: 2L, picture: "updated-pic.png")

        and: "categoryRepository.findById returns empty for the non-existent ID"
        categoryRepository.findById(nonExistentCategoryId) >> Optional.empty()

        when: "updateCategory is called with the non-existent ID"
        categoryServiceHandler.updateCategory(nonExistentCategoryId, categoryUpdateRequest)

        then: "a NotFoundException is thrown"
        thrown(NotFoundException)
    }

    def "updateCategory() method should throw NotFoundException if base category does not exist"() {
        given: "an existing category and a CategoryUpdateRequest with a non-existent baseId"
        def categoryId = random.nextObject(Long)
        def categoryUpdateRequest = new CategoryUpdateRequest(name: "Updated Category", baseId: 999L, picture: "updated-pic.png")
        def existingCategory = new CategoryEntity(id: categoryId, name: "Old Category", baseId: null, picture: "old-pic.png")

        and: "fetchCategoryEntityIfExist returns the existing category"
        categoryRepository.findById(categoryId) >> Optional.of(existingCategory)

        and: "categoryRepository.findById returns empty for the non-existent base ID"
        categoryRepository.findById(categoryUpdateRequest.baseId) >> Optional.empty()

        when: "updateCategory is called"
        categoryServiceHandler.updateCategory(categoryId, categoryUpdateRequest)

        then: "a NotFoundException is thrown for the base category"
        thrown(NotFoundException)
    }

    def "deleteCategory() method must delete a base category and all its subcategories"() {
        given: "a base category with subcategories"
        def categoryId = 1L
        def baseCategory = new CategoryEntity(id: categoryId, name: "Base Category", baseId: null, status: CategoryStatus.ACTIVE)
        def subCategory1 = new CategoryEntity(id: 2L, name: "Sub Category 1", baseId: categoryId, status: CategoryStatus.ACTIVE)
        def subCategory2 = new CategoryEntity(id: 3L, name: "Sub Category 2", baseId: categoryId, status: CategoryStatus.ACTIVE)

        and: "fetchCategoryEntityIfExist returns the base category"
        categoryRepository.findById(categoryId) >> Optional.of(baseCategory)

        and: "findByBaseId returns subcategories"
        categoryRepository.findByBaseId(categoryId) >> [subCategory1, subCategory2]

        when: "deleteCategory is called"
        categoryServiceHandler.deleteCategory(categoryId)

        then: "the base category and subcategories are marked as deleted"
        baseCategory.status == CategoryStatus.DELETED
        subCategory1.status == CategoryStatus.DELETED
        subCategory2.status == CategoryStatus.DELETED

        and: "categoryRepository.saveAll is called with updated entities"
        1 * categoryRepository.saveAll({ List<CategoryEntity> entities ->
            entities.size() == 3 &&
                    entities.any { it.id == baseCategory.id && it.status == CategoryStatus.DELETED } &&
                    entities.any { it.id == subCategory1.id && it.status == CategoryStatus.DELETED } &&
                    entities.any { it.id == subCategory2.id && it.status == CategoryStatus.DELETED }
        })

        and: "cacheServiceHandler.saveCategoriesToCache is called"
        1 * cacheServiceHandler.saveCategoriesToCache()
    }

    def "deleteCategory() method should throw CannotDeleteSubCategoryException if trying to delete a subcategory"() {
        given: "a subcategory with a non-null baseId"
        def categoryId = 2L
        def subCategory = new CategoryEntity(id: categoryId, name: "Sub Category", baseId: 1L, status: CategoryStatus.ACTIVE)

        and: "fetchCategoryEntityIfExist returns the subcategory"
        categoryRepository.findById(categoryId) >> Optional.of(subCategory)

        when: "deleteCategory is called on a subcategory"
        categoryServiceHandler.deleteCategory(categoryId)

        then: "CannotDeleteSubCategoryException is thrown"
        thrown(CannotDeleteSubCategoryException)
    }

    def "deleteCategory() method should throw NotFoundException if category does not exist"() {
        given: "a non-existent category ID"
        def nonExistentCategoryId = 999L

        and: "categoryRepository.findById returns empty"
        categoryRepository.findById(nonExistentCategoryId) >> Optional.empty()

        when: "deleteCategory is called with a non-existent ID"
        categoryServiceHandler.deleteCategory(nonExistentCategoryId)

        then: "NotFoundException is thrown"
        thrown(NotFoundException)
    }
}
