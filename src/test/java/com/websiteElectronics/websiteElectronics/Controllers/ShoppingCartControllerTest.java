package com.websiteElectronics.websiteElectronics.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websiteElectronics.websiteElectronics.Dtos.ShoppingCartDto;
import com.websiteElectronics.websiteElectronics.Services.ShoppingCartService;
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
import java.util.Date;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ShoppingCartController.class)
class ShoppingCartControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetShoppingCart() throws Exception {
        ShoppingCartDto request = new ShoppingCartDto(101, 1, 1, 1, new Date());
        ShoppingCartDto response = new ShoppingCartDto(101, 1, 1, 1, new Date());
        Mockito.when(shoppingCartService.createShoppingCart(request)).thenReturn(response);

        mockMvc.perform(post("/api/shoppingCart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101));

    }

    @Test
    void testUpdateShoppingCart() throws Exception {
        ShoppingCartDto request = new ShoppingCartDto(101, 1, 1, 1, new Date());
        ShoppingCartDto response = new ShoppingCartDto(101, 1, 1, 2, new Date());
        Mockito.when(shoppingCartService.updateShoppingCart(Mockito.eq(101),request)).thenReturn(response);

        mockMvc.perform(put("/api/shoppingCart/101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2));
        
    }

    @Test
    void testGetShoppingCartById() throws Exception{
        ShoppingCartDto response = new ShoppingCartDto(101, 1, 1, 1, new Date());
        Mockito.when(shoppingCartService.getShoppingCartById(101)).thenReturn(response);
        mockMvc.perform(get("/api/shoppingCart/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteShoppingCart() throws Exception{
        Mockito.doNothing().when(shoppingCartService).deleteShoppingCart(101);
        mockMvc.perform(delete("/api/shoppingCart/101"))
                .andExpect(status().isOk());
    }
}
