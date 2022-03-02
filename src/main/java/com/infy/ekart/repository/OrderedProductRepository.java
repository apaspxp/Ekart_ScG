package com.infy.ekart.repository;

import com.infy.ekart.entity.OrderedProduct;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderedProductRepository extends CrudRepository<OrderedProduct, Integer> {
    Optional<List<OrderedProduct>> findByOrderId(Integer orderId);
}
