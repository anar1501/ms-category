package az.ingress.mscategory.model.response;

import az.ingress.mscategory.model.request.CategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySeparationResult {
    private List<CategoryRequest.CategoryDetail> baseCategories;
    private List<CategoryRequest.CategoryDetail> subCategories;
}
