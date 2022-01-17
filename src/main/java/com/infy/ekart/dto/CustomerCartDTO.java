package com.infy.ekart.dto;

import java.util.Set;



public class CustomerCartDTO {
	
	private Integer cartId;
	// ensure not null and validate email
	
	private String customerEmailId;
	// validate aggregation object
	private Set<CartProductDTO> cartProducts;
	
	
	public Integer getCartId() {
		return cartId;
	}
	public void setCartId(Integer cartId) {
		this.cartId = cartId;
	}
	public String getCustomerEmailId() {
		return customerEmailId;
	}
	public void setCustomerEmailId(String customerEmailId) {
		this.customerEmailId = customerEmailId;
	}
	public Set<CartProductDTO> getCartProducts() {
		return cartProducts;
	}
	public void setCartProducts(Set<CartProductDTO> cartProducts) {
		this.cartProducts = cartProducts;
	}
	
	
	
	
	

}
