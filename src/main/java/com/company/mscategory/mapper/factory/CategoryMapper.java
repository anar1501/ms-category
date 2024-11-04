package com.company.mscategory.mapper.factory;

import com.company.mscategory.dao.entity.CategoryEntity;
import com.company.mscategory.model.request.CategoryRequest;
import com.company.mscategory.model.response.CategorySeparationResult;
import com.company.mscategory.model.response.CategoryTreeNodeResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.mscategory.model.enums.CategoryStatus.ACTIVE;

public enum CategoryMapper {
    CATEGORY_MAPPER;

    public CategorySeparationResult separateCategories(List<CategoryRequest.CategoryDetail> categories) {
        var baseCategories = new ArrayList<CategoryRequest.CategoryDetail>();
        var subCategories = new ArrayList<CategoryRequest.CategoryDetail>();
        categories.forEach(categoryDetail -> {
            if (categoryDetail.getBaseId() == null) {
                baseCategories.add(categoryDetail);
            } else
                subCategories.add(categoryDetail);
        });
        return new CategorySeparationResult(baseCategories, subCategories);
    }

    public List<CategoryEntity> mapBaseCategoryDetailToListCategoryEntity(List<CategoryRequest.CategoryDetail> baseCategories) {
        return baseCategories.stream()
                .map(baseCategory -> CategoryEntity.builder()
                        .name(baseCategory.getName())
                        .baseId(null)
                        .picture(baseCategory.getPicture())
                        .status(ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CategoryEntity> mapSubCategoryDetailToListCategoryEntity(List<CategoryRequest.CategoryDetail> subCategories) {
        return subCategories.stream()
                .map(subCategory -> CategoryEntity.builder()
                        .name(subCategory.getName())
                        .baseId(subCategory.getBaseId())
                        .picture(subCategory.getPicture())
                        .status(ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CategoryTreeNodeResponse> buildCategoryTree(List<CategoryEntity> categories) {
        var subCategoryMap = categories.stream()
                .filter(categoryEntity -> categoryEntity.getBaseId() != null)
                .map(CategoryTreeNodeResponse::new)
                .collect(Collectors.groupingBy(CategoryTreeNodeResponse::getBaseId));
        return categories.stream()
                .filter(categoryEntity -> categoryEntity.getBaseId() == null)
                .map(base -> {
                    var baseNode = new CategoryTreeNodeResponse(base);
                    baseNode.setSubCategories(subCategoryMap.get(base.getId()));
                    return baseNode;
                })
                .collect(Collectors.toList());
    }
}

