package ru.diszexuf.streamlive.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.diszexuf.streamlive.dto.CategoryDto;
import ru.diszexuf.streamlive.model.Category;
import ru.diszexuf.streamlive.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public CategoryDto createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    
    public Optional<CategoryDto> updateCategory(Long id, Category updatedCategory) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setName(updatedCategory.getName());
                    category.setDescription(updatedCategory.getDescription());
                    category.setThumbnailUrl(updatedCategory.getThumbnailUrl());
                    return categoryRepository.save(category);
                })
                .map(this::convertToDto);
    }
    
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public CategoryDto convertToDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getThumbnailUrl(),
        );
    }
    
    public void updateViewersCount(Long categoryId, int delta) {
        categoryRepository.findById(categoryId).ifPresent(category -> {
            category.setViewersCount(category.getViewersCount() + delta);
            categoryRepository.save(category);
        });
    }
} 