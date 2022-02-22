package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        properties = {"spring.main.lazy-initialization=true"},
        classes = {CategoryService.class, CategoryServiceImpl.class}
)
public class CategoryServiceTest {
    private final CategoryService categoryService;

    @MockBean
    private CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceTest(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Test
    public void whenFindAll() {
        // given
        final List<CategoryEntity> categories = List.of(
                new CategoryEntity("Категория 1"),
                new CategoryEntity("Категория 2"),
                new CategoryEntity("Категория 3"));
        // when
        when(this.categoryRepository.findAll()).thenReturn(categories);
        final List<CategoryEntity> foundCategory = this.categoryService.findAll();
        // then
        assertEquals(categories, foundCategory);
        verify(this.categoryRepository, times(1)).findAll();
    }

    @Test
    public void whenFindById() {
        // given
        final CategoryEntity categoryEntity = new CategoryEntity("Категория 10");
        // when
        when(this.categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        Optional<CategoryEntity> foundCategory = this.categoryService.findById(1L);
        // then
        assertEquals(Optional.of(categoryEntity), foundCategory);
        verify(this.categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void whenAddCategory() {
        // given
        final CategoryEntity categoryEntity = new CategoryEntity("Категория 100");
        // when
        when(this.categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);
        final CategoryEntity savedCategory = (CategoryEntity)this.categoryService.save(categoryEntity);
        // then
        assertEquals(categoryEntity, savedCategory);
        verify(this.categoryRepository, times(1)).save(categoryEntity);
    }

    @Test
    public void whenUpdateCategory() {
        // when
        this.categoryRepository.updateCategoryEntity(anyLong(), anyString());
        // then
        verify(this.categoryRepository, times(1)).updateCategoryEntity(anyLong(), anyString());
    }

    @Test
    public void whenDeleteCategory() {
        // when
        this.categoryService.delete(anyLong());
        verify(this.categoryRepository, times(1)).deleteById(anyLong());
    }
}
