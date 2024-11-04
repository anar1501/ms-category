package com.company.mscategory.dao.repository;

import com.company.mscategory.dao.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByBaseId(Long categoryId);
}