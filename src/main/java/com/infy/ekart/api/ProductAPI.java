package com.infy.ekart.api;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.infy.ekart.dto.ProductDTO;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.service.CustomerProductService;

@RestController
@RequestMapping(value = "/product-api")
public class ProductAPI {

	@Autowired
	private CustomerProductService customerProductService;

	@Autowired
	private Environment environment;

	final static String REDUCE_QUANTITY_SUCCESSFULL = "Quantity of product was reduced successfully.";
	Log logger = LogFactory.getLog(ProductAPI.class);

	// Get all the product details by calling getAllProducts() of
	// CustomerProductService and return the same

	@GetMapping(value = "/products")
	public ResponseEntity<List<ProductDTO>> getAllProducts() throws EKartException {

		// write your logic here
		logger.info("Entered into method getAllProducts()");
		try {
			List<ProductDTO> productDTOList = customerProductService.getAllProducts();
			logger.info("Number of records fetched from database: " + productDTOList.size());
			return new ResponseEntity<List<ProductDTO>>(productDTOList, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<List<ProductDTO>>(Arrays.asList(new ProductDTO()),HttpStatus.NOT_FOUND);
		}

	}

	// Get the specific product by calling getProductById() of
	// CustomerProductService and return the same
	@GetMapping(value = "/product/{productId}")
	public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer productId) throws EKartException {
		// write your logic here
		logger.info("Entered into method getProductById()");
		try {
			ProductDTO productDTO = customerProductService.getProductById(productId);
			logger.info("Product fetched and the product id is: " + productDTO.getProductId());
			return new ResponseEntity<ProductDTO>(productDTO, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<ProductDTO>(new ProductDTO(),HttpStatus.NOT_FOUND);
		}
	}

	// Reduce the available quantity of product by calling reduceAvailableQuantity()
	// of CustomerProductService and return the message with property as
	// ProductAPI.REDUCE_QUANTITY_SUCCESSFULL
	@PutMapping(value = "/update/{productId}")
	public ResponseEntity<String> reduceAvailableQuantity(@PathVariable Integer productId, @RequestBody String quantity)
			throws EKartException {

		// write your logic here
		logger.info("Entered into method reduceAvailableQuantity()");
		try {
			customerProductService.reduceAvailableQuantity(productId,Integer.valueOf(quantity));
			return new ResponseEntity<String>(ProductAPI.REDUCE_QUANTITY_SUCCESSFULL, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Availability could not be reduced.",HttpStatus.NOT_FOUND);
		}

	}
}
