package com.infy.ekart.repository.test;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.infy.ekart.entity.Product;
import com.infy.ekart.repository.ProductRepository;



@DataJpaTest
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	private Product product;

	@BeforeEach
	public void setUp() {
		product = new Product();
		product.setProductId(1);
	}

	@Test
	void saveValidTest() {
		Product productAfterSave = productRepository.save(new Product());
		Optional<Product> optionalProduct = productRepository.findById(productAfterSave.getProductId());
		Assertions.assertTrue(optionalProduct.isPresent());
	}

	@Test
	void findByIdValidTest() {
		productRepository.save(product);
		Optional<Product> optionalProduct = productRepository.findById(1);
		Assertions.assertTrue(optionalProduct.isPresent());
	}

	@Test
	void findByIdInvalidTest() {
		productRepository.save(product);
		Optional<Product> optionalProduct = productRepository.findById(2);
		Assertions.assertTrue(optionalProduct.isEmpty());
	}

	@Test
	void findAllValidTest() {
		productRepository.save(product);
		productRepository.save(new Product());
		List<Product> products = (List<Product>) productRepository.findAll();
		Assertions.assertEquals(2, products.size());
	}

	@Test
	void findAllInvalidTest() {
		productRepository.save(product);
		productRepository.save(new Product());
		List<Product> products = (List<Product>) productRepository.findAll();
		Assertions.assertNotEquals(0, products.size());
	}
}

