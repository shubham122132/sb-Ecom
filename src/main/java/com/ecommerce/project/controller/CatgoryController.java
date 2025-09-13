package com.ecommerce.project.controller;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.service.CategoryServiceImp;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequestMapping("api")
@RestController
public class CatgoryController {
    @Autowired
    CategoryServiceImp cateService;

    @GetMapping("public/categories")
    public  ResponseEntity<List<Category>> getAllCategories(){
        List<Category> categories = cateService.getAllCategories();
        return new ResponseEntity<>(categories,HttpStatus.OK);
    }
    @PostMapping("public/category")
    public ResponseEntity<String> addCategory(@Valid @RequestBody  Category category){
        try{
            String already = cateService.createCategory(category);
            return new ResponseEntity<>(already,HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(),e.getStatusCode());
        }
    }
    @DeleteMapping("public/category")
    public ResponseEntity<String> delateCategory(@RequestParam Long categoryId){
        String status = cateService.deletCategory(categoryId);
        return new ResponseEntity<>(status,HttpStatus.OK);
    }
    @PutMapping("public/category/{categoryId}")
    public ResponseEntity<String> updateCategory( @Valid @RequestBody Category category ,@PathVariable Long categoryId){

        Category updatedCategory = cateService.updateCategory(category,categoryId);
        return new ResponseEntity<>("Category with category id :"+categoryId+" : "+updatedCategory,HttpStatus.OK);
    }

}
