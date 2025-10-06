package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImp implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDto, Long categoryId) {
        //check if project is already present or not
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Category","CategoryId",categoryId));

        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for (Product value : products){
            if(value.getProductName().equals(productDto.getProductName())){
                isProductNotPresent = false;
                break;
            }
        }
        if(isProductNotPresent){
            Product product = modelMapper.map(productDto,Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        }else{
            throw new ApiException("Product already exist");
        }

    }

    @Override
    public String removeProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product","ProductId",productId));

        productRepository.deleteById(productId);
        return "Product Removed Successfully";
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        //check if product size is 0 or not
        Page<Product> productPage = productRepository.findAll(pageDetails);
        List<Product> products= productPage.getContent();
        if(products.isEmpty()){
            throw new ApiException("no Product exists");
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product->modelMapper.map(product, ProductDTO.class)).toList();
        // prepare response
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }


    @Override
    public ProductResponse searchByCategory(Long categoryId,Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Category","categoryId",categoryId));
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails =PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategory(category,pageDetails);

        List<Product> products = productPage.getContent();
        //        check if product size is 0 or not
        if(products.isEmpty()){
            throw new ApiException("no Product exists");
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product->modelMapper.map(product, ProductDTO.class)).toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;

    }

    @Override
    public ProductResponse searchByKeyword(String keyword,Integer pageNumber,Integer pageSize,String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%',pageDetails);

        List<Product> products = productPage.getContent();
        // check product size
        if(products.isEmpty()){
            throw new ApiException("no Product exists");
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product->modelMapper.map(product, ProductDTO.class)).toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDto, Long productId) {
        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product","ProductId",productId));

        Product product = modelMapper.map(productDto,Product.class);
        savedProduct.setProductName(product.getProductName());
        savedProduct.setDescription(product.getDescription());
        savedProduct.setQuantity(product.getQuantity());
        savedProduct.setDiscount(product.getDiscount());
        savedProduct.setPrice(product.getPrice());
        double specialPrice = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
        savedProduct.setSpecialPrice(specialPrice);
        Product updatedProduct = productRepository.save(savedProduct);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //get product from db if exist
        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product","ProductId",productId));
        // upload images
        
        //get the file name fo uploaded image

        String fileName = fileService.uploadImage(path,image);
        
        //updating the new file name to the product
        savedProduct.setImage(fileName);
         // save updated Product
        Product updatedProduct = productRepository.save(savedProduct);
        //return ProductDTO
        return modelMapper.map(updatedProduct,ProductDTO.class);
    }


}

