package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImp implements CategoryService{
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        Page<Category> categoryPage = categoryRepo.findAll(pageDetails);
        // fetch all categories from database
        List<Category> categories = categoryPage.getContent();
        if(categories.isEmpty()) throw new ApiException("No Category is Created Till Now ");
        // map the list of categories to list of categoryDTOs
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class)).toList();

        // prepare the response
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        //return the response
        return categoryResponse;

    }

    @Override
    public  CategoryDTO createCategory(CategoryDTO categoryDTO) {

        // map the categoryDTO to category
        Category category = modelMapper.map(categoryDTO, Category.class);
        // check if category with the same name already exists before update
        Optional<Category> exist = categoryRepo.findByCategoryName(category.getCategoryName());
        // if category with same name  does not exist then add new category
        if (exist.isEmpty()){
            Category updatedCategory = categoryRepo.save(category);
            return modelMapper.map(updatedCategory, CategoryDTO.class);
        }else {
            throw new ApiException("Category with name "+category.getCategoryName()+" Already exist");
        }
    }


    @Override
    public CategoryDTO deleteCategory(Long categoryId){

        // check if category with id exists or not
        Category removeCategory = categoryRepo.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));
        // delete the category from database
        categoryRepo.delete(removeCategory);
        return modelMapper.map(removeCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO,Long categoryDTOId){

       // check if category with id exists or not
        Category existingCategory = categoryRepo.findById(categoryDTOId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryDTOId));

        // check if category with the same name already exists
        Optional<Category> categoryWithSameName = categoryRepo.findByCategoryName(categoryDTO.getCategoryName());

        if (categoryWithSameName.isPresent() && !categoryWithSameName.get().getCategoryId().equals(categoryDTOId)) {
            throw new ApiException("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
        }
        // update the category name
        existingCategory.setCategoryName(categoryDTO.getCategoryName());
        Category updatedCategory = categoryRepo.save(existingCategory);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }
}