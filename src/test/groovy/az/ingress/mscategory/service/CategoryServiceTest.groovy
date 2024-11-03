package az.ingress.mscategory.service


import az.ingress.mscategory.dao.repository.CategoryRepository
import az.ingress.mscategory.service.concrete.CacheServiceHandler
import az.ingress.mscategory.service.concrete.CategoryServiceHandler
import io.github.benas.randombeans.EnhancedRandomBuilder
import io.github.benas.randombeans.api.EnhancedRandom
import spock.lang.Specification

class CategoryServiceTest extends Specification {
    EnhancedRandom random = EnhancedRandomBuilder.aNewEnhancedRandom()
    CategoryServiceHandler categoryService
    CategoryRepository categoryRepository
    CacheServiceHandler cacheService

    def setup() {
        categoryRepository = Mock()
        cacheService = Mock()
        categoryService = new CategoryServiceHandler(categoryRepository, cacheService)
    }
}
