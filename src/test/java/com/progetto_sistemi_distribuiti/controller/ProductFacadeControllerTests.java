package com.progetto_sistemi_distribuiti.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductFacadeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void categoriesEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Skincare"))
                .andExpect(jsonPath("$[1]").value("Fashion"))
                .andExpect(jsonPath("$[2]").value("Laptops"));
    }

    @Test
    void suggestionsEndpointShouldAcceptCategoryAndProductName() throws Exception {
        mockMvc.perform(get("/api/products/suggestions")
                        .param("category", "Laptops")
                        .param("productName", "Apple MacBook Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topRated").isArray())
                .andExpect(jsonPath("$.bestPrice").isArray())
                .andExpect(jsonPath("$.similar").isArray())
                .andExpect(jsonPath("$.topRated[0].category").value("Laptops"))
                .andExpect(jsonPath("$.bestPrice[0].category").value("Laptops"))
                .andExpect(jsonPath("$.similar[0].category").value("Laptops"));
    }

    @Test
    void productReviewsEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/products/reviews").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
