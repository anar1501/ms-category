package az.ingress.mscategory.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotNull(message = "Categories list cannot be null")
    private List<CategoryDetail> categories;

    @Data
    public static class CategoryDetail {
        @NotBlank(message = "Category name cannot be blank")
        private String name;
        private Long baseId;
        @NotBlank(message = "Category picture cannot be blank")
        private String picture;
    }
}

