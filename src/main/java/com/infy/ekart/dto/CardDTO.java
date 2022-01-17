package com.infy.ekart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDTO {
	@Override
	public String toString() {
		return "CardDTO [cardType=" + cardType + ", cardNumber=" + cardNumber + ", nameOnCard=" + nameOnCard
				+ ", hashCvv=" + hashCvv + ", cvv=" + cvv + ", expiryYear=" + expiryYear + ", expiryMonth="
				+ expiryMonth + ", cardId=" + cardId + ", customerEmailId=" + customerEmailId + "]";
	}
	private String cardType;
	private String cardNumber;
	private String nameOnCard;
	private String hashCvv;
	// ensure not null
	private Integer cvv;
	private String expiryYear;
	private String expiryMonth;
	// ensure not null
	private Integer cardId;
	private String customerEmailId;
	
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getExpiryYear() {
		return expiryYear;
	}
	public void setExpiryYear(String expiryYear) {
		this.expiryYear = expiryYear;
	}
	public String getExpiryMonth() {
		return expiryMonth;
	}
	public void setExpiryMonth(String expiryMonth) {
		this.expiryMonth = expiryMonth;
	}
	public String getNameOnCard() {
		return nameOnCard;
	}
	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}
	public String getCustomerEmailId() {
		return customerEmailId;
	}
	public void setCustomerEmailId(String customerEmailId) {
		this.customerEmailId = customerEmailId;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	
	public Integer getCardId() {
		return cardId;
	}
	public void setCardId(Integer cardId) {
		this.cardId = cardId;
	}
	public String getHashCvv() {
		return hashCvv;
	}
	public void setHashCvv(String hashCvv) {
		this.hashCvv = hashCvv;
	}
	public Integer getCvv() {
		return cvv;
	}
	public void setCvv(Integer cvv) {
		this.cvv = cvv;
	}
	
}
