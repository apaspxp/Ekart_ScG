package com.infy.ekart.api;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.infy.ekart.dto.CartProductDTO;
import com.infy.ekart.dto.OrderDTO;
import com.infy.ekart.dto.TransactionDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.infy.ekart.dto.CardDTO;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.service.PaymentService;

//Add the missing annotations
@RestController
@RequestMapping(value = "/payment-api")

public class PaymentAPI {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private Environment environment;

	@Autowired
	private RestTemplate template;

	Log logger = LogFactory.getLog(PaymentAPI.class);

	// This api will add a new card for particular customer by calling
	// addCustomerCard() of PaymentService, which in turn returns cardId
	// Set the success message with value PaymentAPI.CUSTOMER_CARD_DELETED_SUCCESS
	// appended with cardId
	// and return the same
	//
	@PostMapping(value = "/customer/{customerEmailId:.+}/cards")
	public ResponseEntity<String> addNewCard(@RequestBody CardDTO cardDTO,
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.email.format}") @PathVariable("customerEmailId") String customerEmailId)
			throws EKartException, NoSuchAlgorithmException {
		// write your logic here
		logger.info("Entered into method addNewCard()");
		try {
			Integer cardId = paymentService.addCustomerCard(customerEmailId, cardDTO);
			logger.info("Card with card id " + cardId + " was successfully added for the customer with email id " + customerEmailId);
			return new ResponseEntity<String>(String.valueOf(cardId), HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Card could not be added for the customer.", HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping(value = "/update/card")
	public ResponseEntity<String> updateCustomerCard(@Valid @RequestBody CardDTO cardDTO)
			throws EKartException, NoSuchAlgorithmException {
		logger.info("Recieved request to update  card :" + cardDTO.getCardId() + " of customer : "
				+ cardDTO.getCustomerEmailId());

		paymentService.updateCustomerCard(cardDTO);
		String modificationSuccessMsg = environment.getProperty("PaymentAPI.UPDATE_CARD_SUCCESS");
		return new ResponseEntity<>(modificationSuccessMsg, HttpStatus.OK);

	}

	// Delete the customer cards details by calling deleteCustomerCard()
	// method of PaymentService()
	// Set the success message with value PaymentAPI.CUSTOMER_CARD_DELETED_SUCCESS
	// and return the same
	@DeleteMapping(value = "/customer/{customerEmailId:.+}/card/{cardID}/delete")
	public ResponseEntity<String> deleteCustomerCard(@PathVariable("cardID") Integer cardID,
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.email.format}") @PathVariable("customerEmailId") String customerEmailId)
			throws EKartException {

		// write your logic here
		logger.info("Entered into method deleteCustomerCard()");
		try {
			paymentService.deleteCustomerCard(customerEmailId, cardID);
			return new ResponseEntity<String>("Card with card id " + cardID + " was deleted for the customer with email id " + customerEmailId, HttpStatus.OK);
		}catch (Exception e){
			return new ResponseEntity<String>("Card could not be deleted.", HttpStatus.NOT_FOUND);
		}
	}

	// Get the customer cards details by calling getCardsOfCustomer()
	// method of PaymentService() and return the list of card details obtained
	@GetMapping(value = "/customer/{customerEmailId}/card-type/{cardType}")
	public ResponseEntity<List<CardDTO>> getCardsOfCustomer(@PathVariable String customerEmailId,
			@PathVariable String cardType) throws EKartException {
		// write your logic here
		logger.info("Entered into method getCardsOfCustomer()");
		try {
			List<CardDTO> cardDTOList = paymentService.getCardsOfCustomer(customerEmailId, cardType);
			logger.info("Number of records in the list: " + cardDTOList.size());
			return new ResponseEntity<List<CardDTO>>(cardDTOList,HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<List<CardDTO>>(Arrays.asList(new CardDTO()),HttpStatus.NOT_FOUND);
		}
	}

	// Get the order details of Customer for the given orderId by calling respective
	// API
	// Update the Transaction details with the obtained Order details in above step,
	// along with transaction date and total price
	// Authenticate the transaction details for the given customer by calling
	// authenticatePayment() method of PaymentService
	// Add the transaction details to the database by calling addTransaction()
	// method of PaymentService
	// Update the order status by calling by calling respective API
	// Set the appropriate success message and return the same
	@PostMapping(value = "/customer/{customerEmailId}/order/{orderId}")
	public ResponseEntity<String> payForOrder(
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.email.format}") @PathVariable("customerEmailId") String customerEmailId,
			@NotNull(message = "{orderId.absent") @PathVariable("orderId") Integer orderId,
			@Valid @RequestBody CardDTO cardDTO) throws NoSuchAlgorithmException, EKartException {

		// write your logic here
		logger.info("Entered into method payForOrder()");
		try {
			ResponseEntity<OrderDTO> orderDetailsResponse = template.getForEntity(
					"http://localhost:3333/Ekart/order-api/order/" + orderId,
					OrderDTO.class);
			logger.info("Order details was fetched, order id: " + orderDetailsResponse.getBody().getOrderId() + " order status: " + orderDetailsResponse.getBody().getOrderStatus());
			TransactionDTO transactionDTO = new TransactionDTO();
			transactionDTO.setOrder(orderDetailsResponse.getBody());
			transactionDTO.setTransactionDate(LocalDateTime.now());
			transactionDTO.setTotalPrice(orderDetailsResponse.getBody().getTotalPrice());
			List<CardDTO> cardDTOList = paymentService.getCardsOfCustomer(customerEmailId, orderDetailsResponse.getBody().getPaymentThrough());
			logger.info("Card id: " + cardDTOList.get(0).getCardId());
			transactionDTO.setCard(cardDTOList.get(0));
			logger.info("transactionDTO: " + transactionDTO.getCard().getHashCvv());
			TransactionDTO authenticatedTransactionDTO = paymentService.authenticatePayment(customerEmailId, transactionDTO);
			logger.info("authenticatedTransactionDTO: " + authenticatedTransactionDTO.getTransactionStatus());
			Integer transactionId = paymentService.addTransaction(authenticatedTransactionDTO);
			template.put("http://localhost:3333/Ekart/order-api/order/" + orderId + "/update/order-status","TRANSACTION_SUCCESS");
			return new ResponseEntity<String>("Payment for order with order id " + orderId + " was successfull and the transaction id is " + transactionId, HttpStatus.OK);
		}catch (Exception e){
			e.printStackTrace();
			return new ResponseEntity<String>("Payment failed for order with order id " + orderId, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
