package az.ingress.mscategory.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    @NotBlank(message = "Category name cannot be blank")
    private String name;
    private Long baseId;
    @NotBlank(message = "Category picture cannot be blank")
    private String picture;
}
