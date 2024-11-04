package com.company.mscategory.controller


import com.company.mscategory.exception.ErrorHandler
import com.company.mscategory.exception.NotFoundException
import com.company.mscategory.model.request.CategoryRequest
import com.company.mscategory.model.request.CategoryUpdateRequest
import com.company.mscategory.model.response.CategoryTreeNodeResponse
import com.company.mscategory.service.concrete.CategoryServiceHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import static org.hamcrest.Matchers.containsString
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class CategoryControllerTest extends Specification {
    private CategoryServiceHandler categoryServiceHandler
    private MockMvc mockMvc
    private ObjectMapper objectMapper = new ObjectMapper()

    void setup() {
        categoryServiceHandler = Mock()
        def categoryController = new CategoryController(categoryServiceHandler)
        def validatorFactory = new LocalValidatorFactoryBean()
        validatorFactory.afterPropertiesSet()
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new ErrorHandler())
                .setValidator(validatorFactory)
                .build()
    }

    //GET TEST METHODS
    def "getCategories should return list of categories"() {
        given:
        def categoryResponse = new CategoryTreeNodeResponse(id: 1L, name: "CategoryName", baseId: null, picture: "picture.jpg", subCategories: [])
        def expectedJson = objectMapper.writeValueAsString([categoryResponse])
        categoryServiceHandler.getCategories() >> [categoryResponse]

        when:
        def response = mockMvc.perform(get("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
    }
    //GET TEST METHODS


    //DELETE TEST METHODS
    def "createCategory() method should create categories and return HTTP 201"() {
        given: "a valid CategoryRequest"
        def categoryDetail1 = new CategoryRequest.CategoryDetail(name: "Category 1", baseId: null, picture: "pic1.png")
        def categoryDetail2 = new CategoryRequest.CategoryDetail(name: "Category 2", baseId: 1L, picture: "pic2.png")
        def categoryRequest = new CategoryRequest(categories: [categoryDetail1, categoryDetail2])

        and: "the request body as JSON"
        def requestBody = objectMapper.writeValueAsString(categoryRequest)

        when: "POST request is made to /categories endpoint"
        def result = mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

        then: "the response status is 201 CREATED"
        result.andExpect(status().isCreated())

        and: "categoryService.createCategory is called with the correct parameters"
        1 * categoryServiceHandler.createCategory(_ as CategoryRequest) >> { CategoryRequest request ->
            assert request.categories.size() == 2
            assert request.categories[0].name == "Category 1"
            assert request.categories[1].name == "Category 2"
        }
    }


    def "createCategory() method should return HTTP 400 when categories list is null"() {
        given: "an invalid CategoryRequest with null categories"
        def categoryRequest = new CategoryRequest(categories: null)

        and: "the request body as JSON"
        def requestBody = objectMapper.writeValueAsString(categoryRequest)

        when: "POST request is made to /v1/categories endpoint"
        def result = mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

        then: "the response status is 400 BAD REQUEST"
        result.andExpect(status().isBadRequest())

        and: "categoryService.createCategory is not called"
        0 * categoryServiceHandler.createCategory(_)
    }

    def "createCategory() method should return HTTP 400 when required fields are missing"() {
        given: "an invalid CategoryRequest with missing name and picture"
        def categoryRequest = new CategoryRequest(categories: null)

        and: "the request body as JSON"
        def requestBody = objectMapper.writeValueAsString(categoryRequest)

        when: "POST request is made to /v1/categories endpoint"
        def result = mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

        then: "the response status is 400 BAD REQUEST"
        result.andExpect(status().isBadRequest())

        and: "categoryService.createCategory is not called"
        0 * categoryServiceHandler.createCategory(_)

        and: "response contains validation error details"
        result.andExpect(jsonPath('$.code').value("VALIDATION_EXCEPTION"))
                .andExpect(jsonPath('$.message').value("[categories: Categories list cannot be null]"))
    }

    //UPDATE TEST METHODS
    def "should return HTTP 204 NO_CONTENT when category is successfully updated"() {
        given: "a valid CategoryUpdateRequest"
        def categoryUpdateRequest = new CategoryUpdateRequest(name: "Updated Category", baseId: 1L, picture: "updated-pic.png")
        def requestBody = objectMapper.writeValueAsString(categoryUpdateRequest)

        and: "a valid categoryId"
        def categoryId = 1L

        when: "PUT request is made to /v1/categories/{categoryId} endpoint"
        def result = mockMvc.perform(put("/v1/categories/$categoryId")
                .contentType("application/json")
                .content(requestBody))

        then: "the response status is 204 NO_CONTENT"
        result.andExpect(status().isNoContent())

        and: "categoryService.updateCategory is called with correct parameters"
        1 * categoryServiceHandler.updateCategory(categoryId, _ as CategoryUpdateRequest)
    }
    //CREATE TEST METHODS

    def "should return HTTP 400 with VALIDATION_EXCEPTION code when required fields are missing"() {
        given: "an invalid CategoryUpdateRequest with blank name and picture"
        def categoryUpdateRequest = new CategoryUpdateRequest(name: "", baseId: 1L, picture: "")
        def requestBody = objectMapper.writeValueAsString(categoryUpdateRequest)

        and: "a valid categoryId"
        def categoryId = 1L

        when: "PUT request is made to /v1/categories/{categoryId} endpoint"
        def result = mockMvc.perform(put("/v1/categories/$categoryId")
                .contentType("application/json")
                .content(requestBody))

        then: "the response status is 400 BAD_REQUEST"
        result.andExpect(status().isBadRequest())

        and: "the response contains the expected validation error structure"
        result.andExpect(jsonPath('$.code').value("VALIDATION_EXCEPTION"))
                .andExpect(jsonPath('$.message').value(containsString("name: Category name cannot be blank")))
                .andExpect(jsonPath('$.message').value(containsString("picture: Category picture cannot be blank")))
    }
    //UPDATE TEST METHODS



    //DELETE TEST METHODS
    def "should return HTTP 204 NO_CONTENT when category is successfully deleted"() {
        given: "a valid categoryId"
        def categoryId = 1L

        when: "DELETE request is made to /v1/categories/{categoryId} endpoint"
        def result = mockMvc.perform(delete("/v1/categories/$categoryId"))

        then: "the response status is 204 NO_CONTENT"
        result.andExpect(status().isNoContent())

        and: "categoryService.deleteCategory is called with the correct categoryId"
        1 * categoryServiceHandler.deleteCategory(categoryId)
    }

    def "should return HTTP 404 NOT_FOUND when category does not exist"() {
        given: "a non-existent categoryId"
        def categoryId = 999L

        and: "categoryService throws NotFoundException for the given categoryId"
        categoryServiceHandler.deleteCategory(categoryId) >> { throw new NotFoundException("Category not found", "CATEGORY_NOT_FOUND") }

        when: "DELETE request is made to /v1/categories/{categoryId} endpoint"
        def result = mockMvc.perform(delete("/v1/categories/$categoryId"))

        then: "the response status is 404 NOT_FOUND"
        result.andExpect(status().isNotFound())

        and: "response contains error details from ExceptionResponse"
        result.andExpect(jsonPath('$.code').value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath('$.message').value("Category not found"))
    }
    //DELETE TEST METHODS
}