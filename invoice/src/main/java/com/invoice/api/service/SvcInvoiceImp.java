package com.invoice.api.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.entity.Cart;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.api.repository.RepoCart;
import com.invoice.exception.ApiException;
import com.invoice.configuration.client.ProductClient;

import java.time.LocalDateTime;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;

	@Autowired
  RepoCart repoCart;

	@Autowired
  ProductClient productCl;

	@Override
	public List<Invoice> getInvoices(String rfc) {
		return repo.findByRfcAndStatus(rfc, 1);
	}

	@Override
	public List<Item> getInvoiceItems(Integer invoice_id) {
		return repoItem.getInvoiceItems(invoice_id);
	}

	@Override
	public ApiResponse generateInvoice(String rfc) {
		/*
		 * Requerimiento 5
		 * Implementar el método para generar una factura 
		 */
		//List<Invoice> carrito = getInvoices();
		//if(carrito == null || carrito.isEmpty()) {
		//	throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");
		//} else {
		//	//Ya existe el carrito ahora obtenemos los items.
		//	for(Invoice i : carrito) {
		//		List<Item> aux = getInvoiceItems();
		//		for(Item it:aux) {
		//			//Aquí, generamos los elementos del artículo de la factura.
		//			Double up = it.getUnit_price();
		//			Double to = it.getTotal();
		//			Double tx = it.getTaxes();
		//			Double sb = it.getSubtotal();
		//		}
		//	}
			
		//}
    
    List<Cart> carrito = repoCart.findByRfcAndStatus(rfc, 1);
    List<Item> items = new ArrayList<>();
    // Caso de carro vacio
    if(carrito == null || carrito.isEmpty())
			throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");

    Invoice inv = new Invoice();
    Double st = 0.0, tx = 0.0, tt = 0.0;
    for(Cart c : carrito) {
      // Obtenemos el producto para el precio y actualizamos su stock
      DtoProduct p = productCl.getProduct(c.getGtin()).getBody();
      productCl.updateProductStock(c.getGtin(), c.getQuantity());
      // LLenamos los datos del Item
      Item item = new Item();
      item.setId_invoice(inv.getInvoice_id());
      item.setGtin(c.getGtin());
      item.setQuantity(c.getQuantity());
      item.setUnit_price(p.getPrice());
      item.setTotal(p.getPrice() * c.getQuantity());
      item.setTaxes(item.getTotal() * .16);
      item.setSubtotal(item.getTotal() - item.getTaxes());
      item.setStatus(1);
      items.add(item);
      // repoItem.save(item);
      // try { repoItem.save(item); }
      // catch(Exception e) { throw new ApiException(HttpStatus.BAD_REQUEST, "cannot add item"); }
      // Sumamos los totales
      st += item.getSubtotal(); 
      tx += item.getTaxes();
      tt += item.getTotal();
    }

    // Llenamos los datos de la factura
    inv.setRfc(rfc);
    inv.setSubtotal(st);
    inv.setTaxes(tx);
    inv.setTotal(tt);
    inv.setCreated_at(LocalDateTime.now());
    inv.setStatus(1);
    try { repo.save(inv); }
    catch(Exception e) { throw new ApiException(HttpStatus.BAD_REQUEST, "cannot add invoice"); }

    // Vaciamos el carrito del cliente
    repoCart.clearCart(rfc);

    for(Item i : items) {
      try { repoItem.save(i); }
      catch(Exception e) { throw new ApiException(HttpStatus.BAD_REQUEST, "cannot add item"); }
    }
		
		return new ApiResponse("invoice generated");
	}

}
