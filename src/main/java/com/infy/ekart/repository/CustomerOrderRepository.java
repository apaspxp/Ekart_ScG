package com.infy.ekart.repository;

import org.springframework.data.repository.CrudRepository;

import com.infy.ekart.entity.Order;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends CrudRepository<Order, Integer> {
	// add methods if required

    Optional<List<Order>> findByCustomerEmailId(String customerEmailId);
}
