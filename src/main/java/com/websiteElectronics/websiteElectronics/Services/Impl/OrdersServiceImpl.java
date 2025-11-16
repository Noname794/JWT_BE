package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.OrderStatsDto;
import com.websiteElectronics.websiteElectronics.Dtos.OrdersDto;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Exceptions.NotFoundId;
import com.websiteElectronics.websiteElectronics.Mappers.OrdersMapper;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.OrdersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.PaymentMethodsRepository;
import com.websiteElectronics.websiteElectronics.Repositories.ShippingMethodsRepository;
import com.websiteElectronics.websiteElectronics.Services.InvoicesService;
import com.websiteElectronics.websiteElectronics.Services.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class OrdersServiceImpl implements OrdersService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersServiceImpl.class);

    private final OrdersRepository ordersRepository;
    private final InvoicesService invoicesService;
    private final CustomersRepository customersRepository;
    private final PaymentMethodsRepository paymentMethodsRepository;
    private final ShippingMethodsRepository shippingMethodsRepository;
    private final com.websiteElectronics.websiteElectronics.Repositories.OrderDetailsRepository orderDetailsRepository;
    private final com.websiteElectronics.websiteElectronics.Repositories.ElectronicsRepositorys electronicsRepository;

    @Autowired
    public OrdersServiceImpl(OrdersRepository ordersRepository, 
                            InvoicesService invoicesService,
                            CustomersRepository customersRepository,
                            PaymentMethodsRepository paymentMethodsRepository,
                            ShippingMethodsRepository shippingMethodsRepository,
                            com.websiteElectronics.websiteElectronics.Repositories.OrderDetailsRepository orderDetailsRepository,
                            com.websiteElectronics.websiteElectronics.Repositories.ElectronicsRepositorys electronicsRepository) {
        this.ordersRepository = ordersRepository;
        this.invoicesService = invoicesService;
        this.customersRepository = customersRepository;
        this.paymentMethodsRepository = paymentMethodsRepository;
        this.shippingMethodsRepository = shippingMethodsRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.electronicsRepository = electronicsRepository;
    }

    private Orders findId(int id){
        return ordersRepository.findById(id)
                .orElseThrow(() -> {
                    logger.debug("Could not find order with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find order with id: " + id);
                });

    }

    @Override
    public OrdersDto createOrder(OrdersDto orderDto) {
        Orders order = OrdersMapper.toEntity(orderDto);
        Orders saved = ordersRepository.save(order);

        try {
            com.websiteElectronics.websiteElectronics.Entities.OrderDetails orderDetail = new com.websiteElectronics.websiteElectronics.Entities.OrderDetails();
            orderDetail.setOrderId(saved);

            int productId = ((saved.getId() - 1) % 10) + 1;
            com.websiteElectronics.websiteElectronics.Entities.Products product = electronicsRepository.findById(productId).orElse(null);
            
            if (product != null) {
                orderDetail.setProductId(product);
                orderDetail.setQuantity(1);
                orderDetailsRepository.save(orderDetail);
                logger.info("Auto-created OrderDetail for Order ID: {} with Product ID: {}", saved.getId(), productId);
            } else {
                logger.warn("Could not find product with ID: {} for Order ID: {}", productId, saved.getId());
            }
        } catch (Exception ex) {
            logger.error("Failed to create OrderDetail for Order ID: {}", saved.getId(), ex);
        }

        invoicesService.generateAndSendInvoiceAsync(saved, 43200)
                .thenAccept(invoice -> logger.info("Invoice created and sent for order ID: {}", saved.getId()))
                .exceptionally(ex -> {
                    logger.error("Failed to create/send invoice for order ID: {}", saved.getId(), ex);
                    return null;
                });

        return OrdersMapper.toDto(saved);
    }

    @Override
    public OrdersDto updateOrder(int id, OrdersDto orderDto) {

        Orders order = findId(id);
        order.setOrderDate(orderDto.getOrderDate());
        order.setStatus(orderDto.getStatus());
        order.setTotalAmount(orderDto.getTotalAmount());

        if (orderDto.getCustomerId() > 0) {
            order.setCustomer(customersRepository.findById(orderDto.getCustomerId()).orElse(null));
        }
        if (orderDto.getPaymentMethodId() > 0) {
            order.setPaymentMethod(paymentMethodsRepository.findById(orderDto.getPaymentMethodId()).orElse(null));
        }
        if (orderDto.getShippingMethodId() > 0) {
            order.setShippingMethod(shippingMethodsRepository.findById(orderDto.getShippingMethodId()).orElse(null));
        }
        
        Orders updated = ordersRepository.save(order);
        return OrdersMapper.toDto(updated);
    }

    @Override
    public void deleteOrder(int id) {
        if (!ordersRepository.existsById(id)) {
            throw new NotFoundId("Order not found with id: " + id);
        }
        ordersRepository.deleteById(id);
    }

    @Override
    public OrdersDto getOrderById(int id) {
        Orders order = findId(id);
        return OrdersMapper.toDto(order);
    }

    @Override
    public List<OrdersDto> getAllOrders() {
        return ordersRepository.findAll().parallelStream()
                .map(OrdersMapper::toDto)
                .toList();
    }

    @Override
    public OrderStatsDto getOrderStatsByCustomerId(int customerId) {
        List<Object[]> result = ordersRepository.findOderStatsByCustomerId(customerId);
        logger.debug("customerId: {}", customerId);
        logger.debug("Query result: {}", result);
        long totalOrders = 0L;
        double totalAmountSpent = 0.0;
        if (result != null && !result.isEmpty()) {
            Object[] arr = result.get(0);
            if (arr[0] != null) totalOrders = ((Number) arr[0]).longValue();
            if (arr[1] != null) totalAmountSpent = ((Number) arr[1]).doubleValue();
        }
        return new OrderStatsDto(totalOrders, totalAmountSpent);
    }
}
