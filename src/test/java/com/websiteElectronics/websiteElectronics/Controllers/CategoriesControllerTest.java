package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.CategoriesDto;
import com.websiteElectronics.websiteElectronics.Services.CategoriesService;
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
@WebMvcTest(CategoriesController.class)
public class CategoriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriesService categoriesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateCategory_Success() throws Exception {
        CategoriesDto request = new CategoriesDto(null, "Phone", "Mobile phone", null);
        CategoriesDto response = new CategoriesDto(1, "Phone", "Mobile phone", null);
        Mockito.when(categoriesService.createCategory(Mockito.any(CategoriesDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void testUpdateCategory_Success() throws Exception {
        CategoriesDto request = new CategoriesDto(null, "Tablet", "Tablet device", null);
        CategoriesDto response = new CategoriesDto(1, "Tablet", "Tablet device", null);
        Mockito.when(categoriesService.updateCategory(Mockito.eq(1), Mockito.any(CategoriesDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tablet"));
    }

    @Test
    void testDeleteCategory_Success() throws Exception {
        Mockito.doNothing().when(categoriesService).deleteCategory(1);
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetCategoryById_Success() throws Exception {
        CategoriesDto response = new CategoriesDto(1, "Phone", "Mobile phone", null);
        Mockito.when(categoriesService.getCategoryById(1)).thenReturn(response);
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void testGetAllCategories_Success() throws Exception {
        List<CategoriesDto> responses = Arrays.asList(
                new CategoriesDto(1, "Phone", "Mobile phone", null),
                new CategoriesDto(2, "Tablet", "Tablet device", null)
        );
        Mockito.when(categoriesService.getAllCategories()).thenReturn(responses);
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("Tablet"));
    }
}
