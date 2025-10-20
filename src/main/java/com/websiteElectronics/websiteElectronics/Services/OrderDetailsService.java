package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.OrderDetailsDto;
import com.websiteElectronics.websiteElectronics.Entities.Products;

import java.util.List;

public interface OrderDetailsService {
    List<OrderDetailsDto> lstOrderDetails();
    OrderDetailsDto getOrderDetailsById(int id);
    List<Integer> getProductIdsByCustomerId(Integer orderId);
    List<Products> getElectronicsByCustomerId(Integer customerId);
}
