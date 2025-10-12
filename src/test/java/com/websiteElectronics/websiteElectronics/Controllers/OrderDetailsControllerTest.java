package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.OrderDetailsDto;
import com.websiteElectronics.websiteElectronics.Services.OrderDetailsService;
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
import java.util.Arrays;
import java.util.List;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrderDetailsController.class)
public class OrderDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderDetailsService orderDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllOrderDetails_Success() throws Exception {
        OrderDetailsDto dto1 = new OrderDetailsDto(1, null, null, 2);
        OrderDetailsDto dto2 = new OrderDetailsDto(2, null, null, 1);
        List<OrderDetailsDto> responses = Arrays.asList(dto1, dto2);
        Mockito.when(orderDetailsService.lstOrderDetails()).thenReturn(responses);
        mockMvc.perform(get("/api/orderDetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testGetOrderDetailsById_Success() throws Exception {
        OrderDetailsDto response = new OrderDetailsDto(1, null, null, 2);
        Mockito.when(orderDetailsService.getOrderDetailsById(1)).thenReturn(response);
        mockMvc.perform(get("/api/orderDetails/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(1));
    }


    @Test
    void testGetProductsByCustomerId_Success() throws Exception {
        Mockito.when(orderDetailsService.getElectronicsByCustomerId(1)).thenReturn(Arrays.asList());
        mockMvc.perform(get("/api/orderDetails/getProducts/1"))
                .andExpect(status().isOk());
    }
}
