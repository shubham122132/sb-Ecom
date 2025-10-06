package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @Size(min = 3 , message = "product name must contain atleast 3 charactor")
    private String productName;
    private String image;
    @Size(min = 20 , message = "product name must contain atleast 30 charactor")
    private String description;
    private Integer quantity;
    private double price; // 100
    private double discount; // 30
    private double specialPrice; // 70


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
