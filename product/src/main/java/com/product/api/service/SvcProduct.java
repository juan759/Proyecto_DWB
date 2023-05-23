package com.product.api.service;

import java.util.List;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Product;

public interface SvcProduct {
	public List<DtoProductList> ListProducts(Integer category_id);
	public Product getProduct(String gtin);
	public ApiResponse createProduct(Product in);
	public ApiResponse updateProduct(Product in, Integer id);
	public ApiResponse updateProductStock(String gtin, Integer stock);
	public ApiResponse updateProductCategory(String gtin, Integer category_id);
	public ApiResponse deleteProduct(Integer id);
}
