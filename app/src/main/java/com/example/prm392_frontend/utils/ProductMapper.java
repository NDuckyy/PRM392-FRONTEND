package com.example.prm392_frontend.utils;

import com.example.prm392_frontend.Product;
import com.example.prm392_frontend.models.ProductResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductMapper {

    /**
     * Convert ProductResponse from API to Product model used in app
     */
    public static Product fromResponse(ProductResponse response) {
        if (response == null) {
            return null;
        }

        // Map category name
        String categoryName = response.getCategoryID() != null
                ? response.getCategoryID().getCategoryName()
                : "Uncategorized";

        // For now, we'll use single image URL and create a list with it
        // Since backend only provides single imageURL
        List<String> imageUrls = new ArrayList<>();
        if (response.getImageURL() != null && !response.getImageURL().isEmpty()) {
            imageUrls.add(response.getImageURL());
        }

        // Default values for fields not provided by backend
        double rating = 4.5; // Default rating
        String brand = ""; // Backend doesn't provide brand
        int popularity = 0; // Backend doesn't provide popularity

        // Combine briefDescription and fullDescription
        String description = "";
        if (response.getFullDescription() != null && !response.getFullDescription().isEmpty()) {
            description = response.getFullDescription();
        } else if (response.getBriefDescription() != null) {
            description = response.getBriefDescription();
        }

        Product product = new Product(
                response.getId(),
                response.getProductName() != null ? response.getProductName() : "Unknown Product",
                description,
                response.getPrice(),
                response.getImageURL() != null ? response.getImageURL() : "",
                imageUrls,
                categoryName,
                rating,
                brand,
                popularity,
                response.getTechnicalSpecifications() != null ? response.getTechnicalSpecifications() : ""
        );

        // Set additional backend fields
        product.setProductName(response.getProductName());
        product.setBriefDescription(response.getBriefDescription());
        product.setFullDescription(response.getFullDescription());
        product.setTechnicalSpecifications(response.getTechnicalSpecifications());
        product.setCategoryID(response.getCategoryID());

        // Set provider info
        if (response.getProvider() != null) {
            if (response.getProvider().getUser() != null) {
                product.setProviderId(response.getProvider().getUser().getUsername());
            }
            product.setProviderName(response.getProvider().getProviderName());
        }

        return product;
    }

    /**
     * Convert list of ProductResponse to list of Product
     */
    public static List<Product> fromResponseList(List<ProductResponse> responseList) {
        List<Product> products = new ArrayList<>();
        if (responseList != null) {
            for (ProductResponse response : responseList) {
                Product product = fromResponse(response);
                if (product != null) {
                    products.add(product);
                }
            }
        }
        return products;
    }

    /**
     * Alias for fromResponse - for consistency
     */
    public static Product toProduct(ProductResponse response) {
        return fromResponse(response);
    }
}
