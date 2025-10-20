package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.CustomersDto;
import com.websiteElectronics.websiteElectronics.Services.CustomersService;
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
@WebMvcTest(CustomersController.class)
public class CustomersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomersService customersService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateCustomer_Success() throws Exception {
        CustomersDto request = new CustomersDto(0, "John", "Doe", "john.doe@gmail.com", "password123", "0123456789", "123 Main St", "Hanoi", "HN", "10000", "Vietnam");
        CustomersDto response = new CustomersDto(1, "John", "Doe", "john.doe@gmail.com", "password123", "0123456789", "123 Main St", "Hanoi", "HN", "10000", "Vietnam");
        Mockito.when(customersService.createCustomer(Mockito.any(CustomersDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        CustomersDto request = new CustomersDto(0, "Jane", "Smith", "jane.smith@gmail.com", "password456", "0987654321", "456 Main St", "HCM", "SG", "20000", "Vietnam");
        CustomersDto response = new CustomersDto(1, "Jane", "Smith", "jane.smith@gmail.com", "password456", "0987654321", "456 Main St", "HCM", "SG", "20000", "Vietnam");
        Mockito.when(customersService.updateCustomer(Mockito.eq(1), Mockito.any(CustomersDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void testDeleteCustomer_Success() throws Exception {
        Mockito.doNothing().when(customersService).deleteCustomer(1);
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetCustomerById_Success() throws Exception {
        CustomersDto response = new CustomersDto(1, "John", "Doe", "john.doe@gmail.com", "password123", "0123456789", "123 Main St", "Hanoi", "HN", "10000", "Vietnam");
        Mockito.when(customersService.getCustomerById(1)).thenReturn(response);
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testGetAllCustomers_Success() throws Exception {
        List<CustomersDto> responses = Arrays.asList(
                new CustomersDto(1, "John", "Doe", "john.doe@gmail.com", "password123", "0123456789", "123 Main St", "Hanoi", "HN", "10000", "Vietnam"),
                new CustomersDto(2, "Jane", "Smith", "jane.smith@gmail.com", "password456", "0987654321", "456 Main St", "HCM", "SG", "20000", "Vietnam")
        );
        Mockito.when(customersService.getAllCustomers()).thenReturn(responses);
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }
}
