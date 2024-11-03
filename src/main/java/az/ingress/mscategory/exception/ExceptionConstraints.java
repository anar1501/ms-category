package az.ingress.mscategory.exception;

public interface ExceptionConstraints {
    String UNEXPECTED_EXCEPTION_CODE = "UNEXPECTED_EXCEPTION";
    String UNEXPECTED_EXCEPTION_MESSAGE = "UNEXPECTED_EXCEPTION";
    String CATEGORY_NOT_FOUND_CODE = "CATEGORY_NOT_FOUND";
    String CATEGORY_NOT_FOUND_MESSAGE = "Category not found for id: %s";
    String CANNOT_DELETE_SUBCATEGORY_CODE = "CANNOT_DELETE_SUBCATEGORY";
    String CANNOT_DELETE_SUBCATEGORY_MESSAGE = "Cannot delete subcategory directly for id: %s";
    String VALIDATION_EXCEPTION_CODE = "VALIDATION_EXCEPTION";
    String METHOD_NOT_ALLOWED_CODE = "METHOD_NOT_ALLOWED_CODE";
    String METHOD_NOT_ALLOWED_CODE_MESSAGE = "Method not allowed";
}
