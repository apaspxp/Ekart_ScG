package com.infy.ekart.service.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import com.infy.ekart.entity.Product;
import com.infy.ekart.exception.EKartException;

import com.infy.ekart.repository.ProductRepository;
import com.infy.ekart.service.CustomerProductService;
import com.infy.ekart.service.CustomerProductServiceImpl;

@SpringBootTest
class CustomerProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private CustomerProductService productService = new CustomerProductServiceImpl();

	@Test
	 void getAllProductsValid() throws EKartException {
		List<Product> products = new ArrayList<Product>();
		Product product1 = new Product();
		product1.setProductId(1);
		products.add(product1);
		Product product2 = new Product();
		product2.setProductId(2);
		products.add(product2);
		Mockito.when(productRepository.findAll()).thenReturn(products);
		Assertions.assertEquals(products.size(), productService.getAllProducts().size());

	}

}
