package com.infy.ekart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.infy.ekart.dto.*;
import com.infy.ekart.entity.Order;
import com.infy.ekart.entity.OrderedProduct;
import com.infy.ekart.entity.Product;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.repository.CustomerOrderRepository;
import com.infy.ekart.repository.OrderedProductRepository;
import com.infy.ekart.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//Add the missing annotation
@Service
public class CustomerOrderServiceImpl implements CustomerOrderService {

	@Autowired
	private CustomerOrderRepository orderRepository;

	@Autowired
	private OrderedProductRepository orderedProductRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CustomerService customerService;

	@Override
	public Integer placeOrder(OrderDTO orderDTO) throws EKartException {
		CustomerDTO customerDTO = customerService.getCustomerByEmailId(orderDTO.getCustomerEmailId());
		if (customerDTO.getAddress().isBlank() || customerDTO.getAddress() == null) {
			throw new EKartException("OrderService.ADDRESS_NOT_AVAILABLE");
		}

		Order order = new Order();
		order.setDeliveryAddress(customerDTO.getAddress());
		order.setCustomerEmailId(orderDTO.getCustomerEmailId());
		order.setDateOfDelivery(orderDTO.getDateOfDelivery());
		order.setDateOfOrder(LocalDateTime.now());
		order.setPaymentThrough(PaymentThrough.valueOf(orderDTO.getPaymentThrough()));
		if (order.getPaymentThrough().equals(PaymentThrough.CREDIT_CARD)) {
			order.setDiscount(10.00d);
		} else {
			order.setDiscount(5.00d);

		}

		order.setOrderStatus(OrderStatus.PLACED);
		Double price = 0.0;
		List<OrderedProduct> orderedProducts = new ArrayList<OrderedProduct>();

		for (OrderedProductDTO orderedProductDTO : orderDTO.getOrderedProducts()) {
			if (orderedProductDTO.getProduct().getAvailableQuantity() < orderedProductDTO.getQuantity()) {
				throw new EKartException("OrderService.INSUFFICIENT_STOCK");
			}

			OrderedProduct orderedProduct = new OrderedProduct();
			orderedProduct.setProductId(orderedProductDTO.getProduct().getProductId());
			orderedProduct.setQuantity(orderedProductDTO.getQuantity());
			orderedProducts.add(orderedProduct);
			price = price + orderedProductDTO.getQuantity() * orderedProductDTO.getProduct().getPrice();

		}

		order.setOrderedProducts(orderedProducts);

		order.setTotalPrice(price * (100 - order.getDiscount()) / 100);

		orderRepository.save(order);

		return order.getOrderId();
	}

	// Get the Order details by using the OrderId
	// If not found throw EKartException with message OrderService.ORDER_NOT_FOUND
	// Else return the order details along with the ordered products
	@Override
	public OrderDTO getOrderDetails(Integer orderId) throws EKartException {

		// write your logic here
		Optional<Order> optionalOrder = orderRepository.findById(orderId);
		Order order = optionalOrder.orElseThrow(() -> new EKartException("CustomerOrder.ORDER_NOT_FOUND"));
		OrderDTO orderDTO = new OrderDTO();
		BeanUtils.copyProperties(order, orderDTO);
		orderDTO.setOrderStatus(order.getOrderStatus().toString());
		orderDTO.setPaymentThrough(order.getPaymentThrough().toString());
		List<OrderedProduct> orderedProductList = order.getOrderedProducts();
		List<OrderedProductDTO> orderedProductDTOList =
				orderedProductList.stream().map(orderedProduct -> {
					OrderedProductDTO orderedProductDTO = new OrderedProductDTO();
					BeanUtils.copyProperties(orderedProduct, orderedProductDTO);
					Optional<Product> product = productRepository.findById(orderedProduct.getProductId());
					if (product.isPresent()) {
						ProductDTO productDTO = new ProductDTO();
						productDTO.setProductId(product.get().getProductId());
						productDTO.setBrand(product.get().getBrand());
						productDTO.setCategory(product.get().getCategory());
						productDTO.setPrice(product.get().getPrice());
						productDTO.setAvailableQuantity(product.get().getAvailableQuantity());
						productDTO.setDescription(product.get().getDescription());
						productDTO.setName(product.get().getName());
						orderedProductDTO.setProduct(productDTO);
					}
					return orderedProductDTO;
				}).collect(Collectors.toList());
		orderDTO.setOrderedProducts(orderedProductDTOList);
		return orderDTO;
	}

	// Get the Order details by using the OrderId
	// If not found throw EKartException with message OrderService.ORDER_NOT_FOUND
	// Else update the order status with the given order status
	@Override
	public void updateOrderStatus(Integer orderId, OrderStatus orderStatus) throws EKartException {
		// write your logic here
		Optional<Order> optionalOrder = orderRepository.findById(orderId);
		Order order = optionalOrder.orElseThrow(() -> new EKartException("CustomerOrder.ORDER_NOT_FOUND"));
		order.setOrderStatus(orderStatus);
		orderRepository.save(order);
	}

	// Get the Order details by using the OrderId
	// If not found throw EKartException with message OrderService.ORDER_NOT_FOUND
	// Else check if the order status is already confirmed, if yes then throw
	// EKartException with message OrderService.TRANSACTION_ALREADY_DONE
	// Else update the paymentThrough with the given paymentThrough option
	@Override
	public void updatePaymentThrough(Integer orderId, PaymentThrough paymentThrough) throws EKartException {

		// write your logic here
		Optional<Order> optionalOrder = orderRepository.findById(orderId);
		Order order = optionalOrder.orElseThrow(() -> new EKartException("CustomerOrder.ORDER_NOT_FOUND"));
		if (Objects.nonNull(order) && OrderStatus.CONFIRMED.compareTo(order.getOrderStatus()) == 0){
			throw new EKartException("OrderService.TRANSACTION_ALREADY_DONE");
		}else {
			order.setPaymentThrough(paymentThrough);
			orderRepository.save(order);
		}
	}

	// Get the list of Order details by using the emailId
	// If the list is empty throw EKartException with message
	// OrderService.NO_ORDERS_FOUND
	// Else populate the order details along with ordered products and return that
	// list

	@Override
	public List<OrderDTO> findOrdersByCustomerEmailId(String emailId) throws EKartException {
		// write your logic here
		Optional<List<Order>> optionalOrder = orderRepository.findByCustomerEmailId(emailId);
		List<Order> orders = optionalOrder.orElseThrow(() -> new EKartException("CustomerOrderService.NO_ORDERS_FOUND"));
		List<OrderDTO> orderDTOList = orders.stream().map(order -> {
			Optional<List<OrderedProduct>> orderedProductList = orderedProductRepository.findByOrderId(order.getOrderId());
			List<OrderedProductDTO> orderedProductDTOList = new ArrayList<>();
			if (orderedProductList.isPresent()) {
				order.setOrderedProducts(orderedProductList.get());
				orderedProductDTOList =
						orderedProductList.get().stream().map(orderedProduct -> {
							OrderedProductDTO orderedProductDTO = new OrderedProductDTO();
							BeanUtils.copyProperties(orderedProduct, orderedProductDTO);
							Optional<Product> product = productRepository.findById(orderedProduct.getProductId());
							if (product.isPresent()) {
								ProductDTO productDTO = new ProductDTO();
								productDTO.setProductId(product.get().getProductId());
								productDTO.setBrand(product.get().getBrand());
								productDTO.setCategory(product.get().getCategory());
								productDTO.setPrice(product.get().getPrice());
								productDTO.setAvailableQuantity(product.get().getAvailableQuantity());
								productDTO.setDescription(product.get().getDescription());
								productDTO.setName(product.get().getName());
								orderedProductDTO.setProduct(productDTO);
							}
							return orderedProductDTO;
						}).collect(Collectors.toList());
			}
			OrderDTO orderDTO = new OrderDTO();
			BeanUtils.copyProperties(order,orderDTO);
			orderDTO.setOrderedProducts(orderedProductDTOList);
			return orderDTO;
		}).collect(Collectors.toList());
		return orderDTOList;
	}

}
