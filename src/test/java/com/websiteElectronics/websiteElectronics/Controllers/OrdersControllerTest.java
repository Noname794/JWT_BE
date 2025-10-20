package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.OrdersDto;
import com.websiteElectronics.websiteElectronics.Dtos.OrderStatsDto;
import com.websiteElectronics.websiteElectronics.Services.OrdersService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdersService ordersService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder_Success() throws Exception {
        OrdersDto request = new OrdersDto(0, LocalDateTime.now(), "Delivery", 1000000, 1, 1, 1);
        OrdersDto response = new OrdersDto(1, LocalDateTime.now(), "Delivery", 1000000, 1, 1, 1);
        Mockito.when(ordersService.createOrder(Mockito.any(OrdersDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateOrder_Success() throws Exception {
        OrdersDto request = new OrdersDto(0, LocalDateTime.now(), "SHIPPED", 2000000, 1, 1, 1);
        OrdersDto response = new OrdersDto(1, LocalDateTime.now(), "SHIPPED", 2000000, 1, 1, 1);
        Mockito.when(ordersService.updateOrder(Mockito.eq(1), Mockito.any(OrdersDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void testDeleteOrder_Success() throws Exception {
        Mockito.doNothing().when(ordersService).deleteOrder(1);
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        OrdersDto response = new OrdersDto(1, LocalDateTime.now(), "Delivery", 1000000, 1, 1, 1);
        Mockito.when(ordersService.getOrderById(1)).thenReturn(response);
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetAllOrders_Success() throws Exception {
        List<OrdersDto> responses = Arrays.asList(
                new OrdersDto(1, LocalDateTime.now(), "Delivery", 1000000, 1, 1, 1),
                new OrdersDto(2, LocalDateTime.now(), "SHIPPED", 2000000, 1, 1, 1)
        );
        Mockito.when(ordersService.getAllOrders()).thenReturn(responses);
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].status").value("SHIPPED"));
    }

    @Test
    void testGetOrderStats_Success() throws Exception {
        OrderStatsDto stats = new OrderStatsDto(5L, 5000000.0);
        Mockito.when(ordersService.getOrderStatsByCustomerId(1)).thenReturn(stats);
        mockMvc.perform(get("/api/orders/stats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.totalAmountSpent").value(5000000.0));
    }
}
