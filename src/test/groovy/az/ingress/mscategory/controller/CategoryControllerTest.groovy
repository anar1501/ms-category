package az.ingress.mscategory.controller

import az.ingress.mscategory.exception.ErrorHandler
import az.ingress.mscategory.service.concrete.CategoryServiceHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class CategoryControllerTest extends Specification {
    private CategoryServiceHandler categoryServiceHandler
    private MockMvc mockMvc
    private ObjectMapper objectMapper = new ObjectMapper()

    void setup() {
        categoryServiceHandler = Mock()
        def categoryController = new CategoryController(categoryServiceHandler)
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new ErrorHandler())
                .build()
    }
}