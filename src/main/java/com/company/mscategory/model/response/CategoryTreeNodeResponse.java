package com.company.mscategory.model.response;

import com.company.mscategory.dao.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeNodeResponse {
    private Long id;
    private String name;
    private Long baseId;
    private String picture;
    @ToString.Exclude
    private List<CategoryTreeNodeResponse> subCategories;

    public CategoryTreeNodeResponse(CategoryEntity entity) {
        this(entity.getId(), entity.getName(), entity.getBaseId(), entity.getPicture(), null);
    }
}

