package com.infy.ekart.entity;

import java.util.Set;

//map class to table
public class CustomerCart {

	// generate automatically
	private Integer cartId;

	private String customerEmailId;

	// establish one to many relationship
	private Set<CartProduct> cartProducts;

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

	public Set<CartProduct> getCartProducts() {
		return cartProducts;
	}

	public void setCartProducts(Set<CartProduct> cartProducts) {
		this.cartProducts = cartProducts;
	}

}
