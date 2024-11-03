package az.ingress.mscategory.util.cache;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public interface CacheConstraints {
    String CACHE_KEY = "ms-category:categories:";
    Long CACHE_EXPIRATION_COUNT = 1L;
    TemporalUnit CACHE_EXPIRATION_UNIT = ChronoUnit.DAYS;
}
