package com.infy.ekart.repository;



import org.springframework.data.repository.CrudRepository;

import com.infy.ekart.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, String> {

	String findByPhoneNumber(String phoneNumber);

	// add methods if required
	Optional<Customer> findByEmailId(String emailId);

}
