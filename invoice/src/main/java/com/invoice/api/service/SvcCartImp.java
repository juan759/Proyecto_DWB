package com.invoice.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoCustomer;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.repository.RepoCart;
import com.invoice.configuration.client.CustomerClient;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcCartImp implements SvcCart {

	@Autowired
	RepoCart repo;
	
	@Autowired
	CustomerClient customerCl;
	
	@Autowired
	ProductClient productCl;
	
	@Override
	public List<Cart> getCart(String rfc) {
		return repo.findByRfcAndStatus(rfc,1);
	}

	@Override
	public ApiResponse addToCart(Cart cart) {
		if(!validateCustomer(cart.getRfc()))
			throw new ApiException(HttpStatus.BAD_REQUEST, "customer does not exist");
			
		/*
		 * Requerimiento 3
		 * Validar que el GTIN exista. Si existe, asignar el stock del producto a la variable product_stock 
		 */
		Integer product_stock = 0; // cambiar el valor de cero por el stock del producto recuperado de la API Product 
		if (!validateProduct(cart.getGtin()))
			throw new ApiException(HttpStatus.BAD_REQUEST,"product does not exist");
		try {
			//Aquí creamos la respuesta que nos permitira obtener de la API product el stock
			//del producto a agregar. Y actualizamos nuestro product stock.
			product_stock = cart.getQuantity();
			ResponseEntity<DtoProduct> response = productCl.getProduct(cart.getGtin());
			if(response.getStatusCode()==HttpStatus.OK) {
				Integer st = response.getBody().getStock();
				product_stock = product_stock + st;
			}
		}catch(Exception E){
			throw new ApiException(HttpStatus.BAD_REQUEST,"cannot get product stock");
		}
		
		if(cart.getQuantity() > product_stock) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "invalid quantity");
		}
		
		/*
		 * Requerimiento 4
		 * Validar si el producto ya había sido agregado al carrito para solo actualizar su cantidad
		 */
		
		//Aquí, cart.getGtin() == null está mal, porque en realidad buscamos que el Gtin
		// esté en el carrito y no que solo exista.
		if(cart.getGtin( )== null) {
			cart.setStatus(1);
			repo.save(cart);
		}else{
			try {
				ResponseEntity<DtoProduct> response = productCl.getProduct(cart.getGtin());
				Integer stock = cart.getQuantity()+response.getBody().getStock();
				cart.setQuantity(stock);
			}catch(Exception E) {
				throw new ApiException(HttpStatus.BAD_REQUEST,"cannot update cart stock");
			}
		}
		
		
		return new ApiResponse("item added");
	}

	@Override
	public ApiResponse removeFromCart(Integer cart_id) {
		if (repo.removeFromCart(cart_id) > 0)
			return new ApiResponse("item removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "item cannot be removed");
	}

	@Override
	public ApiResponse clearCart(String rfc) {
		if (repo.clearCart(rfc) > 0)
			return new ApiResponse("cart removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "cart cannot be removed");
	}
	
	private boolean validateCustomer(String rfc) {
		try {
			ResponseEntity<DtoCustomer> response = customerCl.getCustomer(rfc);
			if(response.getStatusCode() == HttpStatus.OK)
				return true;
			else
				return false;
		}catch(Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "unable to retrieve customer information");
		}
	}
	
	private boolean validateProduct(String gtin) {
		try {
			ResponseEntity<DtoProduct> response = productCl.getProduct(gtin);
			if(response.getStatusCode()==HttpStatus.OK)
				return true;
			else
				return false;
		}catch(Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST,"unable to retrieve product information");
		}
	}
}
