package com.financeapp.service;

import com.financeapp.model.Category;
import com.financeapp.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void seedCategories() {
        List<String> defaults = List.of(
                "FOOD", "RENT", "UTILITIES", "TRAVEL",
                "ENTERTAINMENT", "HEALTHCARE", "EDUCATION", "OTHER"
        );
        for (String name : defaults) {
            if (categoryRepository.findByCategoryName(name).isEmpty()) {
                categoryRepository.save(Category.builder().categoryName(name).build());
            }
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }
}