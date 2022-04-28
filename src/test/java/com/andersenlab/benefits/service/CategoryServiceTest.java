package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.CategoryEntity;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        final Page<CategoryEntity> pageOfCategory = new PageImpl<>(categories);
        // when
        when(this.categoryRepository.findAll(PageRequest.of(0, 3))).thenReturn(pageOfCategory);
        final Page<CategoryEntity> foundCategory = this.categoryService.findAll(PageRequest.of(0, 3));
        // then
        assertEquals(pageOfCategory, foundCategory);
        verify(this.categoryRepository, times(1)).findAll(PageRequest.of(0, 3));
    }

    @Test
    public void whenFindById() {
        // given
        final CategoryEntity categoryEntity = new CategoryEntity("Категория 10");
        // when
        when(this.categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        final CategoryEntity foundCategory = this.categoryService.findById(1L);
        // then
        assertEquals(categoryEntity, foundCategory);
        verify(this.categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void whenAddCategory() {
        // given
        final CategoryEntity categoryEntity = new CategoryEntity("Категория 100");
        // when
        when(this.categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);
        final CategoryEntity savedCategory = this.categoryService.save(categoryEntity);
        // then
        assertEquals(categoryEntity, savedCategory);
        verify(this.categoryRepository, times(1)).save(categoryEntity);
    }

    @Test
    public void whenUpdateCategorySuccess() {
        // given
        final CategoryEntity category = new CategoryEntity("Category55");
        category.setId(10L);
        // when
        when(this.categoryRepository.findByTitle(anyString())).thenReturn(Optional.empty());
        when(this.categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        this.categoryService.update(category.getId(), category);
        // then
        verify(this.categoryRepository, times(1)).save(eq(category));
    }

    @Test
    public void whenUpdateCategoryTheSameTitile() {
        // given
        final CategoryEntity category = new CategoryEntity("Category55");
        category.setId(10L);
        final CategoryEntity categorytheSameTitle = new CategoryEntity("Category55");
        categorytheSameTitle.setId(11L);
        // when
        when(this.categoryRepository.findByTitle(anyString())).thenReturn(Optional.of(categorytheSameTitle));
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.categoryService.update(category.getId(), category));
        // then
        assertTrue(exception.getMessage().contains(category.getTitle() + " already exist in database"));
    }

    @Test
    public void whenDeleteCategory() {
        // given
        final CategoryEntity category = new CategoryEntity("Category55");
        category.setId(10L);
        // when
        when(this.categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        this.categoryService.delete(10L);
        // then
        verify(this.categoryRepository, times(1)).delete(eq(category));
    }
}
