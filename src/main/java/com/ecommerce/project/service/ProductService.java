package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDto, Long categoryId);

    String removeProduct(Long productId);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category);

    ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse searchByKeyword(String keyword,Integer pageNumber,Integer pageSize,String sortBy, String SortOrder);

    ProductDTO updateProduct(ProductDTO productDto, Long productId);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
}
