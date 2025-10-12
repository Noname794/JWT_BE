package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.ProductsDto;
import com.websiteElectronics.websiteElectronics.Services.ElectronicsService;
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
@WebMvcTest(ElectronicsController.class)
public class ElectronicsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElectronicsService electronicsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateElectronics_Success() throws Exception {
        ProductsDto request = new ProductsDto(0, "iPhone 15", "Apple smartphone", 25000000, 10, "img.jpg", null, null);
        ProductsDto response = new ProductsDto(1, "iPhone 15", "Apple smartphone", 25000000, 10, "img.jpg", null, null);
        Mockito.when(electronicsService.createElectronics(Mockito.any(ProductsDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/electronics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"));
    }

    @Test
    void testUpdateElectronics_Success() throws Exception {
        ProductsDto request = new ProductsDto(0, "iPad Pro", "Apple tablet", 30000000, 5, "ipad.jpg", null, null);
        ProductsDto response = new ProductsDto(1, "iPad Pro", "Apple tablet", 30000000, 5, "ipad.jpg", null, null);
        Mockito.when(electronicsService.updateElectronics(Mockito.eq(1), Mockito.any(ProductsDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/electronics/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPad Pro"));
    }

    @Test
    void testDeleteElectronics_Success() throws Exception {
        Mockito.doNothing().when(electronicsService).deleteElectronics(1);
        mockMvc.perform(delete("/api/electronics/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetElectronicsById_Success() throws Exception {
        ProductsDto response = new ProductsDto(1, "iPhone 15", "Apple smartphone", 25000000, 10, "img.jpg", null, null);
        Mockito.when(electronicsService.getElectronicsById(1)).thenReturn(response);
        mockMvc.perform(get("/api/electronics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"));
    }

    @Test
    void testGetAllElectronics_Success() throws Exception {
        List<ProductsDto> responses = Arrays.asList(
                new ProductsDto(1, "iPhone 15", "Apple smartphone", 25000000, 10, "img.jpg", null, null),
                new ProductsDto(2, "iPad Pro", "Apple tablet", 30000000, 5, "ipad.jpg", null, null)
        );
        Mockito.when(electronicsService.getAllElectronics()).thenReturn(responses);
        mockMvc.perform(get("/api/electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("iPad Pro"));
    }
}
