package com.invoice.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;

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
		List<Invoice> carrito = repo.findByRfcAndStatus(rfc,1);
		if(carrito ==null) {
			throw new ApiException(HttpStatus.NOT_FOUND,"cart has no items");
		}else {
			//Ya existe el carrito ahora obtenemos los items.
			for(Invoice i:carrito) {
				List<Item> aux = repoItem.getInvoiceItems(i.getInvoice_id());
				for(Item it:aux) {
					//Aquí, generamos los elementos del artículo de la factura.
					Double up = it.getUnit_price();
					Double to = it.getTotal();
					Double tx = it.getTaxes();
					Double sb = it.getSubtotal();
				}
			}
			
		}
		
		return new ApiResponse("invoice generated");
	}

}
