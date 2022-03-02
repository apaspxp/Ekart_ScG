package com.infy.ekart.api;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.infy.ekart.dto.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.infy.ekart.exception.EKartException;
import com.infy.ekart.service.CustomerOrderService;

@RestController
@RequestMapping(value = "/order-api")
public class OrderAPI {

	@Autowired
	private CustomerOrderService orderService;

	@Autowired
	private Environment environment;

	@Autowired
	private RestTemplate template;

	static Log logger = LogFactory.getLog(OrderAPI.class);

	@PostMapping(value = "/place-order")
	public ResponseEntity<String> placeOrder(@Valid @RequestBody OrderDTO order) throws EKartException {

		ResponseEntity<CartProductDTO[]> cartProductDTOsResponse = template.getForEntity(
				"http://localhost:3333/Ekart/cart-api/customer/" + order.getCustomerEmailId() + "/products",
				CartProductDTO[].class);

		CartProductDTO[] cartProductDTOs = cartProductDTOsResponse.getBody();
		template.delete("http://localhost:3333/Ekart/cart-api/customer/" + order.getCustomerEmailId() + "/products");
		List<OrderedProductDTO> orderedProductDTOs = new ArrayList<>();

		for (CartProductDTO cartProductDTO : cartProductDTOs) {
			OrderedProductDTO orderedProductDTO = new OrderedProductDTO();
			orderedProductDTO.setProduct(cartProductDTO.getProduct());
			orderedProductDTO.setQuantity(cartProductDTO.getQuantity());
			orderedProductDTOs.add(orderedProductDTO);
		}
		order.setOrderedProducts(orderedProductDTOs);

		Integer orderId = orderService.placeOrder(order);
		String modificationSuccessMsg = environment.getProperty("OrderAPI.ORDERED_PLACE_SUCCESSFULLY");

		return new ResponseEntity<String>(modificationSuccessMsg + orderId, HttpStatus.OK);

	}

	// Get the Order details for the given orderId
	// For each OrderedProduct in the order,get the Product details by calling
	// respective Product API
	// Update the Order details with the returned Product details and return the
	// same
	@GetMapping(value = "order/{orderId}")
	public ResponseEntity<OrderDTO> getOrderDetails(
			@NotNull(message = "{orderId.absent}") @PathVariable Integer orderId) throws EKartException {
		// write your logic here
		logger.info("Entered into getOrderDetails()");
		try {
			return new ResponseEntity<OrderDTO>(orderService.getOrderDetails(orderId), HttpStatus.OK);
		}catch (Exception e){
			return new ResponseEntity<OrderDTO>(new OrderDTO(),HttpStatus.NOT_FOUND);
		}

	}

	// Get the list of Order details for the given customerEmailId by calling
	// findOrdersByCustomerEmailId() method of CustomerOrderService
	// For each OrderedProduct in the order,get the Product details by calling
	// respective Product API
	// Update the Order details with the returned Product details and return the
	// same

	@GetMapping(value = "customer/{customerEmailId}/orders")
	public ResponseEntity<List<OrderDTO>> getOrdersOfCustomer(
			@NotNull(message = "{email.absent}") @PathVariable String customerEmailId) throws EKartException {

		// write your logic here
		List<OrderDTO> orderDTOList = orderService.findOrdersByCustomerEmailId(customerEmailId);
		return new ResponseEntity<List<OrderDTO>>(orderDTOList,HttpStatus.OK);
	}

	@PutMapping(value = "order/{orderId}/update/order-status")
	public void updateOrderAfterPayment(@NotNull(message = "{orderId.absent}") @PathVariable Integer orderId,
			@RequestBody String transactionStatus) throws EKartException {
		if (transactionStatus.equals("TRANSACTION_SUCCESS")) {
			orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
			OrderDTO orderDTO = orderService.getOrderDetails(orderId);
			for (OrderedProductDTO orderedProductDTO : orderDTO.getOrderedProducts()) {

				template.put("http://localhost:3333/Ekart/product-api/update/"
						+ orderedProductDTO.getProduct().getProductId(), orderedProductDTO.getQuantity());

			}

		} else {
			orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
		}
	}

	// Based on the payment-through (debit card or credit card) , call the
	// updatePaymentThrough() method of CustomerOrderService by passing the
	// respective card type
	@PutMapping(value = "order/{orderId}/update/payment-through")
	public void updatePaymentOption(@NotNull(message = "{orderId.absent}") @PathVariable Integer orderId,
			@RequestBody String paymentThrough) throws EKartException {
		// write your logic here
		logger.info("Entered into method updatePaymentOption()");
		orderService.updatePaymentThrough(orderId, PaymentThrough.valueOf(paymentThrough));
	}

}
