package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstant;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api")
@RestController
public class CatgoryController {
    @Autowired
    private CategoryService cateService;


    @GetMapping("/public/categories")
    public  ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstant.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConstant.SORT_CATEGORY_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder",defaultValue = AppConstant.SORT_DIR,required = false) String sortOrder
    ){

        CategoryResponse categoryResponse = cateService.getAllCategories(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
    }
    @PostMapping("/public/category")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody  CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO = cateService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
    }
    @DeleteMapping("/public/category/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){

        CategoryDTO category = cateService.deleteCategory(categoryId);
        return new ResponseEntity<>(category,HttpStatus.OK);

    }
    @PutMapping("/public/category/{categoryDTOId}")
    public ResponseEntity<String> updateCategory( @Valid @RequestBody CategoryDTO categoryDTO ,@PathVariable Long categoryDTOId){

        CategoryDTO updatedCategory = cateService.updateCategory(categoryDTO,categoryDTOId);
        return new ResponseEntity<>("Category with category id :"+categoryDTOId+" : "+updatedCategory,HttpStatus.OK);

    }

}
