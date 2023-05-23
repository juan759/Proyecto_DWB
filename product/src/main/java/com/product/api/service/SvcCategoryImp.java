package com.product.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.api.entity.Category;
import com.product.api.dto.ApiResponse;
import com.product.exception.ApiException;

@Service
public class SvcCategoryImp implements SvcCategory {

    @Autowired
    RepoCategory repo;

    @Autowired
    RepoProduct repoProduct;
    
    @Override
    public List<Category> getCategories() throws Exception {
        return repo.findByStatus(1);
    }
    
    @Override
    public Category getCategory(Integer category_id) {
        Category c = repo.findByCategoryId(category_id);
        if (c == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "category does not exists");
        else
            return c;
    }
    
    @Override
    public ApiResponse createCategory(Category category) {
        Category cc = repo.findByCategory(category.getCategory());
        Category ca = repo.findByAcronym(category.getAcronym());

        if(cc == null && ca == null) {
            repo.createCategory(category.getCategory(), category.getAcronym());
            return new ApiResponse("category created");
        }

        // No estoy seguro de como tratar con este caso porque no se si
        // deberia de activar la categoria si coincide por acronimo
        // por ahora lo dejo como que sí lo hace
        if(ca != null && ca.getStatus() == 0) {
            repo.activateCategory(ca.getCategory_id());
            return new ApiResponse("category has been activated");
        }

        if(cc != null && cc.getStatus() == 0) {
            repo.activateCategory(cc.getCategory_id());
            return new ApiResponse("category has been activated");
        }

        throw new ApiException(HttpStatus.BAD_REQUEST, "category alredy exists");
    }
    
    @Override
    public ApiResponse updateCategory(Integer category_id, Category category) {
        Category c = repo.findAllByCategoryId(category_id);

        if(c == null) throw new ApiException(HttpStatus.BAD_REQUEST, "category does not exist");
        if(c.getStatus() == 0) throw new ApiException(HttpStatus.BAD_REQUEST, "category is not active");

        c = repo.findByCategory(category.getCategory());
        if(c != null) throw new ApiException(HttpStatus.BAD_REQUEST, "category alredy exists");

        c = repo.findByAcronym(category.getAcronym());
        if(c != null) throw new ApiException(HttpStatus.BAD_REQUEST, "category alredy exists");

        repo.updateCategory(category_id, category.getCategory(), category.getAcronym());
        return new ApiResponse("category updated");
    }
    
    @Override
    public ApiResponse deleteCategory(Integer category_id) {
        Category c = repo.findByCategoryId(category_id);
        if(c == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "category does not exist");
        if(repoProduct.findByCategory_id(category_id) != null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "category cannot be removed if it has products");

        repo.deleteById(category_id);
        return new ApiResponse("category removed");
    }
}
