package com.infy.ekart.repository;

import org.springframework.data.repository.CrudRepository;

import com.infy.ekart.entity.Card;

import java.util.List;
import java.util.Optional;


public interface CardRepository extends CrudRepository<Card, Integer> {

	// add methods if required
    Optional<List<Card>> findByCardTypeAndCustomerEmailId(String cardType, String customerEmailId);

    Optional<List<Card>> findByCustomerEmailId(String customerEmailId);

    Optional<List<Card>> findByCardIDAndCustomerEmailId(Integer cardID, String customerEmailId);
}
