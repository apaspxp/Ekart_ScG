package com.infy.ekart.api;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.infy.ekart.dto.CartProductDTO;
import com.infy.ekart.dto.CustomerCartDTO;
import com.infy.ekart.dto.ProductDTO;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.service.CustomerCartService;

//Add the missing annotation
@CrossOrigin
@RestController
@RequestMapping(value = "/cart-api")
public class CartAPI {

	@Autowired
	private CustomerCartService customerCartService;

	@Autowired
	private Environment environment;

	@Autowired
	private RestTemplate template;

	Log logger = LogFactory.getLog(CartAPI.class);

	// Add product to customer cart by calling addProductToCart() method of
	// CustomerCartService which in turn return the cartId
	// Set the appropriate success message with cartId and return the same
	@PostMapping(value = "/products")
	public ResponseEntity<String> addProductToCart(@RequestBody CustomerCartDTO customerCartDTO) throws EKartException {
		// write your logic here
		logger.info("Entered into addProductToCart()");
		try {
			Integer cartId = customerCartService.addProductToCart(customerCartDTO);
			logger.info("Cart id: " + cartId);
			return new ResponseEntity<String>("Product was added to cart with cart id: " + cartId, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Product could not be added.", HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping(value = "/customer/{customerEmailId}/products")
	public ResponseEntity<Set<CartProductDTO>> getProductsFromCart(
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.customeremail.format}") @PathVariable("customerEmailId") String customerEmailId)
			throws EKartException {
		logger.info("Received a request to get products details from " + customerEmailId + " cart");

		Set<CartProductDTO> cartProductDTOs = customerCartService.getProductsFromCart(customerEmailId);
		for (CartProductDTO cartProductDTO : cartProductDTOs) {
			logger.info("Product call");
			ProductDTO productDTO = template.getForEntity(
					"http://localhost:3333/Ekart/product-api/product/" + cartProductDTO.getProduct().getProductId(),
					ProductDTO.class).getBody();
			cartProductDTO.setProduct(productDTO);
			logger.info("Product complete");

		}
		return new ResponseEntity<Set<CartProductDTO>>(cartProductDTOs, HttpStatus.OK);

	}

	// Delete the product details from the cart of customer by calling
	// deleteProductFromCart() method of CustomerCartService
	// Set the appropriate success or failure message and return the same
	@DeleteMapping(value = "/customer/{customerEmailId}/product/{productId}")
	public ResponseEntity<String> deleteProductFromCart(
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.customeremail.format}") @PathVariable("customerEmailId") String customerEmailId,
			@NotNull(message = "{invalid.email.format}") @PathVariable("productId") Integer productId)
			throws EKartException {
		// write your logic here
		logger.info("Entered into deleteProductFromCart()");
		try {
			customerCartService.deleteProductFromCart(customerEmailId, productId);
			logger.info("Product was deleted from cart.");
			return new ResponseEntity<String>("Product with product id " + productId + " was deleted from cart of the customer with email id " + customerEmailId, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Product could not be deleted. ", HttpStatus.NOT_FOUND);
		}
	}

	// Update the quantity of product details available in the cart of customer by
	// calling modifyQuantityOfProductInCart() method of CustomerCartService
	// Set the appropriate success or failure message and return the same
	@PutMapping(value = "/customer/{customerEmailId}/product/{productId}")
	public ResponseEntity<String> modifyQuantityOfProductInCart(
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.customeremail.format}") @PathVariable("customerEmailId") String customerEmailId,
			@NotNull(message = "{invalid.email.format}") @PathVariable("productId") Integer productId,
			@RequestBody String quantity) throws EKartException {
		// write your logic here
		logger.info("Entered into method modifyQuantityOfProductInCart()");
		try {
			customerCartService.modifyQuantityOfProductInCart(customerEmailId,productId,Integer.valueOf(quantity));
			logger.info("Cart was updated successfully.");
			return new ResponseEntity<String>("Cart was updated successfully with product id " + productId + " of the customer with email id " + customerEmailId, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Cart could not be updated.", HttpStatus.NOT_FOUND);
		}

	}

	// Delete all the products from the cart of customer by calling
	// deleteAllProductsFromCart() method of CustomerCartService
	// Set the appropriate success or failure message and return the same
	@DeleteMapping(value = "/customer/{customerEmailId}/products")
	public ResponseEntity<String> deleteAllProductsFromCart(
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.customeremail.format}") @PathVariable("customerEmailId") String customerEmailId)
			throws EKartException {
		// write your logic here
		logger.info("Entered into method deleteAllProductsFromCart().");
		try {
			customerCartService.deleteAllProductsFromCart(customerEmailId);
			logger.info("Cart was cleared successfully.");
			return new ResponseEntity<String>("All the products for the customer with email id " + customerEmailId + " were deleted from cart successfully.", HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Product deletion failed.", HttpStatus.NOT_FOUND);
		}
	}

}
