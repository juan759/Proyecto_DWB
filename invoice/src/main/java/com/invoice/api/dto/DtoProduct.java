package com.invoice.api.dto;

/*
 * Requerimiento 3
 * Agregar atributos de clase para la validación del producto
 */
public class DtoProduct {
	
	private Integer stock;
	
	private String gtin;

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}
	
}
