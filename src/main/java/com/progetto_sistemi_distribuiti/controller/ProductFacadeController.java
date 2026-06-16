package com.progetto_sistemi_distribuiti.controller;

import com.progetto_sistemi_distribuiti.model.*;
import com.progetto_sistemi_distribuiti.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// CONSEGNA #1.1: Progettare le API lato server per un servizio che si comporta come un Remote Facade per il resto della parte server 
@RestController
@RequestMapping("/api/products")
public class ProductFacadeController {
    private final ProductService service;
    private final UserSession userSession;

    public ProductFacadeController(ProductService service, UserSession userSession) {
        this.service = service;
        this.userSession = userSession;
    }

// REQUISITI BASE (Accessibili a tutti)
    // CONSEGNA #2.1: Elencare le categorie di prodotti 
    @GetMapping("/categories")
    public List<String> getCategories() {
        return service.getCategories();
    }

    // CONSEGNA #1.4: Usare il design pattern Request Batch per dare all’utente suggerimenti (per scoprire dati utili)
    // Il request Batch permetterà di mostrare contemporaneamente ognuno di queste consegne: 
    // CONSEGNA #2.2: Fornire i prodotti con il miglior rating per una data categoria 
    // CONSEGNA #2.4: Trovare i prodotti con il miglior prezzo di una categoria
    // CONSEGNA #2.6: Fornire prodotti simili della stessa categoria confrontandone i nomi 
    @GetMapping("/suggestions") //sintassi: products/suggestions?category=miaCategoria&productName=nomeProdotto
    public SuggestionsBatchDTO getBatchSuggestions(@RequestParam String category,
                                                   @RequestParam(required = false) String productName) {
        List<ProductDTO> topRated = service.getTopRatedByCategory(category);
        ProductDTO bestPriceProduct = service.getBestPriceByCategory(category);
        String nameForSimilarity = chooseNameForSimilarity(productName, topRated);

        List<ProductDTO> bestPrice = buildBestPriceList(bestPriceProduct);
        List<ProductDTO> similarProducts = buildSimilarProductsList(category, nameForSimilarity);

        return new SuggestionsBatchDTO(
            topRated,
            bestPrice,
            similarProducts
        );
    }

    @GetMapping("/top-rated") //sintassi: products/top-rated?category=miaCategoria
    public List<ProductDTO> topRatedByCategory(@RequestParam String category) {
        return service.getTopRatedByCategory(category);
    }

    @GetMapping("/best-price") //sintassi: products/best-price?category=miaCategoria
    public ProductDTO bestPriceByCategory(@RequestParam String category) {
        return service.getBestPriceByCategory(category);
    }

    @GetMapping("/similar") //sintassi: products/similar?category=miaCategoria&productName=nomeProdotto
    public List<ProductDTO> similarByCategoryAndName(@RequestParam String category,
                                                     @RequestParam String productName) {
        return service.getSimilarProducts(category, productName);
    }

    // CONSEGNA #2.3: Trovare i prodotti in base a una parola chiave 
    @GetMapping("/search") //sintassi: products/search?keyword=miaKeyword
    public List<ProductDTO> search(@RequestParam String keyword) {
        userSession.addSearchKeyword(keyword); // Salva lo stato nella sessione dell'utente
        return service.searchByKeyword(keyword);
    }

    // Ottieni la sessione dell'utente
    @GetMapping("/my-history")
    public List<String> getMySearchHistory() {
        return userSession.getSearchHistory();
    }
    
    // CONSEGNA #2.5: Fornire le review per un prodotto
    @GetMapping("/reviews") //sintassi: products/reviews?category=miaCategoria
    public List<String> getReviewsByProduct(@RequestParam Long id) {
        return service.getProductReviews(id);
    }
    
    // CONSEGNA #2.7: Fornire un raggruppamento di prodotti in base a fasce di prezzo
    @GetMapping("/price-ranges")
    public Map<String, List<ProductDTO>> priceRanges() {
        return service.getPriceRanges();
    }

    
// Accessibili solo ad alcuni tipi di utenti
    // Solo il REVISORE può aggiungere recensioni
    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('REVISORE')")
    public String addReview(@PathVariable Long id, @RequestBody String review) {
        service.addReview(id, review);
        return "Recensione aggiunta con successo!";
    }

    // Solo l'AMMINISTRATORE può modificare i prezzi
    @PutMapping("/{id}/price")
    @PreAuthorize("hasRole('AMMINISTRATORE')")
    public String updatePrice(@PathVariable Long id, @RequestParam double price) {
        service.updatePrice(id, price);
        return "Prezzo aggiornato dall'Amministratore!";
    }

    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AMMINISTRATORE')")
    public Product getProductDetail(@PathVariable Long id) {
        return service.getProductById(id);
    }

    @GetMapping("/{id}/similar")
    public List<ProductDTO> similarByProduct(@PathVariable Long id) {
        return service.getSimilarProducts(id);
    }

    private String chooseNameForSimilarity(String requestedProductName, List<ProductDTO> topRated) {
        if (requestedProductName != null && !requestedProductName.isBlank()) {
            return requestedProductName;
        }

        if (topRated.isEmpty()) {
            return null;
        }

        return topRated.get(0).getName();
    }

    private List<ProductDTO> buildBestPriceList(ProductDTO bestPriceProduct) {
        if (bestPriceProduct == null) {
            return List.of();
        }

        return List.of(bestPriceProduct);
    }

    private List<ProductDTO> buildSimilarProductsList(String category, String productName) {
        if (productName == null) {
            return List.of();
        }

        return service.getSimilarProducts(category, productName);
    }
}
