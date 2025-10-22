package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CategoryResponse;
import com.example.prm392_frontend.models.CreateOrUpdateProductRequest;
import com.example.prm392_frontend.models.ProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ProductApi {
    /**
     * Get all products
     */
    @GET("/api/products")
    Call<ApiResponse<List<ProductResponse>>> getAllProducts();

    /**
     * Get product by ID
     */
    @GET("/api/products/{id}")
    Call<ApiResponse<ProductResponse>> getProductById(@Path("id") int id);

    /**
     * Create new product (requires authentication)
     */
    @POST("/api/products")
    Call<ApiResponse<ProductResponse>> createProduct(@Body CreateOrUpdateProductRequest request);

    /**
     * Update existing product (requires authentication)
     */
    @PUT("/api/products/{id}")
    Call<ApiResponse<ProductResponse>> updateProduct(@Path("id") int id, @Body CreateOrUpdateProductRequest request);

    /**
     * Delete product (requires authentication)
     */
    @DELETE("/api/products/{id}")
    Call<ApiResponse<String>> deleteProduct(@Path("id") int id);

    /**
     * Get all categories
     */
    @GET("/api/categories")
    Call<ApiResponse<List<CategoryResponse>>> getAllCategories();
}
