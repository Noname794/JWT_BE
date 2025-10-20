package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.OrderStatsDto;
import com.websiteElectronics.websiteElectronics.Dtos.OrdersDto;
import java.util.List;

public interface OrdersService {
    OrdersDto createOrder(OrdersDto orderDto);
    OrdersDto updateOrder(int id, OrdersDto orderDto);
    void deleteOrder(int id);
    OrdersDto getOrderById(int id);
    List<OrdersDto> getAllOrders();
    OrderStatsDto getOrderStatsByCustomerId(int customerId);
}
