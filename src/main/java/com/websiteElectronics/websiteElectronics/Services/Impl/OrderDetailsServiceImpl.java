package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.OrderDetailsDto;
import com.websiteElectronics.websiteElectronics.Entities.Products;
import com.websiteElectronics.websiteElectronics.Entities.OrderDetails;
import com.websiteElectronics.websiteElectronics.Mappers.OrderDetailsMapper;
import com.websiteElectronics.websiteElectronics.Repositories.ElectronicsRepositorys;
import com.websiteElectronics.websiteElectronics.Repositories.OrderDetailsRepository;
import com.websiteElectronics.websiteElectronics.Services.OrderDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderDetailsServiceImpl implements OrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsServiceImpl.class);

    private final OrderDetailsRepository orderDetailsRepository;

    private final ElectronicsRepositorys electronicsRepositorys;

    @Autowired
    public OrderDetailsServiceImpl(OrderDetailsRepository orderDetailsRepository, ElectronicsRepositorys electronicsRepositorys) {
        this.orderDetailsRepository = orderDetailsRepository;
        this.electronicsRepositorys = electronicsRepositorys;
    }

    private OrderDetails findId(int id) {
        return orderDetailsRepository.findById(id)
                .orElseThrow(() -> {
                    logger.debug("Could not find order details with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find order details with id: " + id);
                });
    }

    @Override
    public List<OrderDetailsDto> lstOrderDetails() {
        List<OrderDetails> lst = orderDetailsRepository.findAll();
        return lst.parallelStream().map(OrderDetailsMapper::toDto).toList();
    }

    @Override
    public OrderDetailsDto getOrderDetailsById(int id) {
        OrderDetails orderDetails = findId(id);
        return OrderDetailsMapper.toDto(orderDetails);
    }

    public List<Integer> getProductIdsByCustomerId(Integer customerId) {
        return orderDetailsRepository.findProductIdsByCustomerId(customerId);
    }

    public List<Products> getElectronicsByCustomerId(Integer customerId) {
        List<Integer> productIds = orderDetailsRepository.findProductIdsByCustomerId(customerId);
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return electronicsRepositorys.findByIdIn(productIds);
    }
}
