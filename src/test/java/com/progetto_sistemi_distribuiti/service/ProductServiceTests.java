package com.progetto_sistemi_distribuiti.service;

import com.progetto_sistemi_distribuiti.model.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ProductServiceTests {

    @Autowired
    private ProductService service;

    @Test
    void categoriesShouldContainLoadedDatasets() {
        List<String> categories = service.getCategories();

        assertTrue(categories.contains("Skincare"));
        assertTrue(categories.contains("Fashion"));
        assertTrue(categories.contains("Laptops"));
    }

    @Test
    void topRatedShouldReturnAtMostFiveProductsFromRequestedCategory() {
        List<ProductDTO> products = service.getTopRatedByCategory("Laptops");

        assertFalse(products.isEmpty());
        assertTrue(products.size() <= 5);
        assertTrue(products.stream().allMatch(product -> "Laptops".equals(product.getCategory())));
    }

    @Test
    void topRatedShouldBeSortedByRatingDescending() {
        List<ProductDTO> products = service.getTopRatedByCategory("Fashion");

        for (int i = 1; i < products.size(); i++) {
            assertTrue(products.get(i - 1).getRating() >= products.get(i).getRating());
        }
    }

    @Test
    void bestPriceShouldBelongToRequestedCategory() {
        ProductDTO product = service.getBestPriceByCategory("Fashion");

        assertNotNull(product);
        assertEquals("Fashion", product.getCategory());
        assertTrue(product.getPrice() >= 0);
    }

    @Test
    void similarProductsShouldUseProductNameAndCategory() {
        List<ProductDTO> products = service.getSimilarProducts("Laptops", "Apple MacBook Pro");

        assertFalse(products.isEmpty());
        assertTrue(products.size() <= 10);
        assertTrue(products.stream().allMatch(product -> "Laptops".equals(product.getCategory())));
        assertTrue(products.stream().anyMatch(product -> product.getName().toLowerCase().contains("macbook")));
    }

    @Test
    void unknownProductShouldHaveNoReviews() {
        List<String> reviews = service.getProductReviews(999999L);

        assertTrue(reviews.isEmpty());
    }
}
