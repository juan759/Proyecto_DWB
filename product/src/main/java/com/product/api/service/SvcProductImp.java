package com.product.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Product;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.api.repository.RepoProductList;
import com.product.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

	@Autowired
	RepoProduct repo;
	
	@Autowired
	RepoProductList repoProductList;
	
	@Autowired
	RepoCategory repoCategory;

	@Override
	public List<DtoProductList> ListProducts(Integer category_id) {
		return repoProductList.getProducts(category_id); // Siempre devuelve status 200
	}

	@Override
	public Product getProduct(String gtin) {
		Product product = repo.findByGtin(gtin);
		if (product != null) {
			// product.setCategory(repoCategory.getCategory(product.getCategory_id()));
			product.setCategory(repoCategory.findByCategoryId(product.getCategory_id()));
			return product;
		}else
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
	}

	@Override
	public ApiResponse createProduct(Product in) {
		in.setStatus(1);

		if(repoCategory.findByCategoryId(in.getCategory_id()) == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");

		try {
			repo.save(in);
		} catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("gtin")) {
				Product p = repo.findAllByGtin(in.getGtin());
				if(p.getStatus() == 1) {
					updateProduct(in, p.getProduct_id());
					return new ApiResponse("product activated");
				} else
					throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
			}
			if (e.getLocalizedMessage().contains("product"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
			if (e.getLocalizedMessage().contains("category"))
				throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		}

		return new ApiResponse("product created");
	}

	@Override
	public ApiResponse updateProduct(Product in, Integer id) {
		Integer updated = 0;
		try {
			updated = repo.updateProduct(id, in.getGtin(), in.getProduct(), in.getDescription(), in.getPrice(), in.getStock(), in.getCategory_id());
		} catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("gtin"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
			if (e.getLocalizedMessage().contains("product"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
			if (e.getLocalizedMessage().contains("category"))
				throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		}
		if(updated == 0)
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
		else
			return new ApiResponse("product updated");
	}

	@Override
	public ApiResponse deleteProduct(Integer id) {
		if (repo.deleteProduct(id) > 0)
			return new ApiResponse("product removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "product cannot be deleted");
	}

	@Override
	public ApiResponse updateProductStock(String gtin, Integer stock) {
		Product product = getProduct(gtin);
		if(stock > product.getStock())
			throw new ApiException(HttpStatus.BAD_REQUEST, "stock to update is invalid");
		
		repo.updateProductStock(gtin, product.getStock() - stock);
		return new ApiResponse("product stock updated");
	}

	@Override
	public ApiResponse updateProductCategory(String gtin, Integer category_id) {
		if(repoCategory.findByCategoryId(category_id) == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		
		if(repo.updateProductCategory(gtin, category_id) > 0)
			return new ApiResponse("product category updated");
		else
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exists");
	}
}
