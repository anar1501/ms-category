package az.ingress.mscategory.service.concrete;

import az.ingress.mscategory.dao.entity.CategoryEntity;
import az.ingress.mscategory.dao.repository.CategoryRepository;
import az.ingress.mscategory.service.abstracts.CacheService;
import az.ingress.mscategory.util.cache.CacheUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static az.ingress.mscategory.util.cache.CacheConstraints.CACHE_EXPIRATION_COUNT;
import static az.ingress.mscategory.util.cache.CacheConstraints.CACHE_EXPIRATION_UNIT;
import static az.ingress.mscategory.util.cache.CacheConstraints.CACHE_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceHandler implements CacheService {
    private final CacheUtil cacheUtil;
    private final CategoryRepository categoryRepository;

    @Async
    @CircuitBreaker(name = "redisCacheBreaker", fallbackMethod = "fallbackSaveToCache")
    @Retry(name = "redisCacheRetry", fallbackMethod = "fallbackSaveToCache")
    @Override
    public CompletableFuture<List<CategoryEntity>> saveCategoriesToCache() {
        List<CategoryEntity> categoryEntityList = categoryRepository.findAll();
        cacheUtil.saveToCache(CACHE_KEY, categoryEntityList, CACHE_EXPIRATION_COUNT, CACHE_EXPIRATION_UNIT);
        log.info("Categories saved to cache with key: {}", CACHE_KEY);
        return CompletableFuture.completedFuture(categoryEntityList);
    }

    @CircuitBreaker(name = "redisCacheBreaker", fallbackMethod = "fallbackGetFromCache")
    @Override
    public List<CategoryEntity> getCategoriesFromCache() {
        return cacheUtil.getBucket(CACHE_KEY);
    }

    @Override
    public void fallbackSaveToCache(List<CategoryEntity> categoryEntityList, Throwable throwable) {
        log.error("Failed to save to cache due to Redis outage. Circuit breaker triggered.", throwable);
    }

    @Override
    public List<CategoryEntity> fallbackGetFromCache(Throwable throwable) {
        log.error("Failed to get from cache due to Redis outage. Circuit breaker triggered.", throwable);
        return List.of();
    }
}
