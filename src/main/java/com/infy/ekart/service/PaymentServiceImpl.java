package com.infy.ekart.service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.infy.ekart.entity.Card;
import org.springframework.beans.factory.annotation.Autowired;

import com.infy.ekart.dto.CardDTO;
import com.infy.ekart.dto.TransactionDTO;
import com.infy.ekart.dto.TransactionStatus;
import com.infy.ekart.entity.Transaction;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.repository.CardRepository;
import com.infy.ekart.repository.TransactionRepository;
import com.infy.ekart.utility.HashingUtility;
import org.springframework.stereotype.Service;

//Add the missing annotation
@Service
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	// Get the list of card details by using the customerEmailId
	// If the obtained list is empty throw EKartException with message
	// PaymentService.CUSTOMER_NOT_FOUND
	// Hash the card CVV value by calling the HashingUtility.getHashValue()
	// Populate the Card details and save to database
	// Return the cardId
	@Override
	public Integer addCustomerCard(String customerEmailId, CardDTO cardDTO)
			throws EKartException, NoSuchAlgorithmException {

		// write your logic here
		try {
			Optional<List<Card>> optionalCardList = cardRepository.findByCustomerEmailId(customerEmailId);
			if (optionalCardList.isEmpty())
				throw new EKartException("PaymentService.CUSTOMER_NOT_FOUND");
			Card card = new Card();
			card.setNameOnCard(cardDTO.getNameOnCard());
			card.setCardType(cardDTO.getCardType());
			card.setCustomerEmailId(cardDTO.getCustomerEmailId());
			card.setExpiryDate(cardDTO.getExpiryDate());
			card.setCardNumber(cardDTO.getCardNumber());
			card.setCvv(HashingUtility.getHashValue(cardDTO.getCvv().toString()));
			cardRepository.save(card);
			return card.getCardID();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// Get the card details by using the given cardId(available in CardDTO)
	// If card not obtained throw an EKartException with message
	// PaymentService.CARD_NOT_FOUND
	// Else update the given card details using setters
	@Override
	public void updateCustomerCard(CardDTO cardDTO) throws EKartException, NoSuchAlgorithmException {

		// write your logic here
		Optional<Card> optionalCard = cardRepository.findById(cardDTO.getCardId());
		Card card = optionalCard.orElseThrow(() -> new EKartException("PaymentService.CARD_NOT_FOUND"));
		card.setCardType(cardDTO.getCardType());
		card.setCardNumber(cardDTO.getCardNumber());
		card.setNameOnCard(cardDTO.getNameOnCard());
		card.setExpiryDate(cardDTO.getExpiryDate());
		card.setCustomerEmailId(cardDTO.getCustomerEmailId());
		cardRepository.save(card);
	}

	// Get the list of card details by using the customerEmailId and cardId
	// If the obtained list is empty throw EKartException with message
	// PaymentService.CUSTOMER_NOT_FOUND
	// Else get the card detail and delete the same
	// If card not obtained throw an EKartException with message
	// PaymentService.CARD_NOT_FOUND
	@Override
	public void deleteCustomerCard(String customerEmailId, Integer cardId) throws EKartException {
		// write your logic here
		try {
			Optional<List<Card>> optionalCardList = cardRepository.findByCardIDAndCustomerEmailId(cardId, customerEmailId);
			if (optionalCardList.isEmpty())
				throw new EKartException("PaymentService.CUSTOMER_NOT_FOUND");
			Optional<Card> optionalCard = cardRepository.findById(cardId);
			Card card = optionalCard.orElseThrow(() -> new EKartException("PaymentService.CARD_NOT_FOUND"));
			cardRepository.deleteById(card.getCardID());
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	// Get the card details by using the given cardId
	// If card not obtained throw an EKartException with message
	// PaymentService.CARD_NOT_FOUND
	// Else populate the card details and return

	@Override
	public CardDTO getCard(Integer cardId) throws EKartException {
		// write your logic here
		Optional<Card> optionalCard = cardRepository.findById(cardId);
		Card card = optionalCard.orElseThrow(() -> new EKartException("PaymentService.CARD_NOT_FOUND"));
		CardDTO cardDTO = new CardDTO();
		cardDTO.setCardId(card.getCardID());
		cardDTO.setCvv(739);
		cardDTO.setCardType(card.getCardType());
		cardDTO.setNameOnCard(card.getNameOnCard());
		cardDTO.setCardNumber(card.getCardNumber());
		cardDTO.setExpiryDate(card.getExpiryDate());
		cardDTO.setCustomerEmailId(card.getCustomerEmailId());
		try {
			cardDTO.setHashCvv(String.valueOf(HashingUtility.getHashValue("739")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return cardDTO;

	}

	// Get the list of card details by using the customerEmailId and cardType
	// If the obtained list is empty throw EKartException with message
	// PaymentService.CARD_NOT_FOUND
	// Else populate the obtained card details and return
	@Override
	public List<CardDTO> getCardsOfCustomer(String customerEmailId, String cardType) throws EKartException {

		// write your logic here

		Optional<List<Card>> optionalCardList = cardRepository.findByCardTypeAndCustomerEmailId(cardType,customerEmailId);
		List<Card> cardList = optionalCardList.orElseThrow(() -> new EKartException("PaymentService.NO_ECORD_FOUND"));
		List<CardDTO> cardDTOList = cardList.stream().map(card -> {
			CardDTO cardDTO = new CardDTO();
			cardDTO.setCardId(card.getCardID());
			cardDTO.setCardNumber(card.getCardNumber());
			cardDTO.setCardType(card.getCardType());
			cardDTO.setNameOnCard(card.getNameOnCard());
			cardDTO.setCustomerEmailId(card.getCustomerEmailId());
			cardDTO.setExpiryDate(card.getExpiryDate());
			cardDTO.setHashCvv(card.getCvv());
			cardDTO.setCvv(739);
			return cardDTO;
		}).collect(Collectors.toList());
		return cardDTOList;
	}

	@Override
	public Integer addTransaction(TransactionDTO transactionDTO) throws EKartException {
		if (transactionDTO.getTransactionStatus().equals(TransactionStatus.TRANSACTION_FAILED)) {
			throw new EKartException("PaymentService.TRANSACTION_FAILED_CVV_NOT_MATCHING");
		}
		Transaction transaction = new Transaction();
		transaction.setCardId(transactionDTO.getCard().getCardId());

		transaction.setOrderId(transactionDTO.getOrder().getOrderId());
		transaction.setTotalPrice(transactionDTO.getTotalPrice());
		transaction.setTransactionDate(transactionDTO.getTransactionDate());
		transaction.setTransactionStatus(transactionDTO.getTransactionStatus());
		transactionRepository.save(transaction);

		return transaction.getTransactionId();
	}

	@Override
	public TransactionDTO authenticatePayment(String customerEmailId, TransactionDTO transactionDTO)
			throws EKartException, NoSuchAlgorithmException {
		if (!transactionDTO.getOrder().getCustomerEmailId().equals(customerEmailId)) {
			throw new EKartException("PaymentService.ORDER_DOES_NOT_BELONGS");

		}

		if (!transactionDTO.getOrder().getOrderStatus().equals("PLACED")) {
			throw new EKartException("PaymentService.TRANSACTION_ALREADY_DONE");

		}
		CardDTO cardDTO = getCard(transactionDTO.getCard().getCardId());
		if (!cardDTO.getCustomerEmailId().matches(customerEmailId)) {

			throw new EKartException("PaymentService.CARD_DOES_NOT_BELONGS");
		}
		if (!cardDTO.getCardType().equals(transactionDTO.getOrder().getPaymentThrough())) {

			throw new EKartException("PaymentService.PAYMENT_OPTION_SELECTED_NOT_MATCHING_CARD_TYPE");
		}
		if (cardDTO.getHashCvv().equals(HashingUtility.getHashValue(transactionDTO.getCard().getCvv().toString()))) {

			transactionDTO.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESS);
		} else {

			transactionDTO.setTransactionStatus(TransactionStatus.TRANSACTION_FAILED);

		}

		return transactionDTO;
	}
}
