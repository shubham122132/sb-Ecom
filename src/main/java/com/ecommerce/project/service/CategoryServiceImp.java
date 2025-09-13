package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImp implements CategoryService{
    @Autowired
    private CategoryRepository categoryRepo;

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        if(categories.isEmpty()) throw new ApiException("No Category is Created Till Now ");
        return categories;
    }

    @Override
    public String createCategory(Category category) {
        Optional<Category> exist = categoryRepo.findByCategoryName(category.getCategoryName());
        if (exist.isEmpty()){
            categoryRepo.save(category);
            return "Category "+category.getCategoryName()+" add successfully";
        }else {
            throw new ApiException("Category with name "+category.getCategoryName()+" Already exist");
        }
    }
    @Override
    public String deletCategory(Long categoryId){

        Category savedCategory = categoryRepo.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        categoryRepo.delete(savedCategory);
        return "Category of id "+categoryId+"removed.";
    }

    @Override
    public Category updateCategory(Category category,Long categoryId){

        Category savedCategory = categoryRepo.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        category.setCategoryId(categoryId);
        return categoryRepo.save(category);
    }
}