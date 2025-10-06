package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CategoryDTO {
    private Long CategoryID;
    @NotBlank
    @Size(min = 4, message = "Category Name must contain 5 characters")
    private String CategoryName;
}
