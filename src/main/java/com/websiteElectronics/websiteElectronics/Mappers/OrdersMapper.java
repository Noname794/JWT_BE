package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.OrdersDto;
import com.websiteElectronics.websiteElectronics.Entities.Orders;

import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.PaymentMethodsRepository;
import com.websiteElectronics.websiteElectronics.Repositories.ShippingMethodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrdersMapper {
    
    private static CustomersRepository customersRepository;
    private static PaymentMethodsRepository paymentMethodsRepository;
    private static ShippingMethodsRepository shippingMethodsRepository;
    
    @Autowired
    public OrdersMapper(CustomersRepository customersRepository,
                        PaymentMethodsRepository paymentMethodsRepository,
                        ShippingMethodsRepository shippingMethodsRepository) {
        OrdersMapper.customersRepository = customersRepository;
        OrdersMapper.paymentMethodsRepository = paymentMethodsRepository;
        OrdersMapper.shippingMethodsRepository = shippingMethodsRepository;
    }
    
    public static OrdersDto toDto(Orders order) {
        if (order == null) return null;
        return new OrdersDto(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCustomer() != null ? order.getCustomer().getId() : 0,
                order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : 0,
                order.getShippingMethod() != null ? order.getShippingMethod().getId() : 0
        );
    }

    public static Orders toEntity(OrdersDto dto) {
        if (dto == null) return null;
        Orders entity = new Orders();
        entity.setId(dto.getId());
        entity.setOrderDate(dto.getOrderDate());
        entity.setStatus(dto.getStatus());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setCustomer(customersRepository.findById(dto.getCustomerId()).orElse(null));
        entity.setPaymentMethod(paymentMethodsRepository.findById(dto.getPaymentMethodId()).orElse(null));
        entity.setShippingMethod(shippingMethodsRepository.findById(dto.getShippingMethodId()).orElse(null));
        return entity;
    }
}
