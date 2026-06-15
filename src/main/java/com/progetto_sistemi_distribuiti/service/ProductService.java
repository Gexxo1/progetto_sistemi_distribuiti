package com.progetto_sistemi_distribuiti.service;

import com.progetto_sistemi_distribuiti.model.Product;
import com.progetto_sistemi_distribuiti.model.ProductDTO;
import com.progetto_sistemi_distribuiti.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final int MAX_SIMILAR_PRODUCTS = 10;

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // Utility per convertire Entita in DTO
    private ProductDTO toDTO(Product product) {
        return new ProductDTO(product.id, product.name, product.category, product.price, product.rating);
    }

    public List<String> getCategories() {
        return repository.findAll().stream()
                .map(product -> product.category)
                .distinct()
                .toList();
    }

    public List<ProductDTO> getTopRatedByCategory(String category) {
        return repository.findAll().stream()
                .filter(product ->
                        product.category.equalsIgnoreCase(category))
                .sorted((first, second) ->
                        Double.compare(second.rating, first.rating))
                .limit(5)
                .map(this::toDTO)
                .toList();
    }

    public ProductDTO getBestPriceByCategory(String category) {
        return repository.findAll().stream()
                .filter(product -> product.category.equalsIgnoreCase(category))
                .min(Comparator.comparingDouble(product -> product.price))
                .map(this::toDTO)
                .orElse(null);
    }

    public List<ProductDTO> getSimilarProducts(Long productId) {
        Product product = getProductById(productId);
        if (product == null) {
            return List.of();
        }

        return getSimilarProducts(product.category, product.name, product.id);
    }

    public List<ProductDTO> getSimilarProducts(String category, String productName) {
        return getSimilarProducts(category, productName, null);
    }

    private List<ProductDTO> getSimilarProducts(String category, String productName, Long excludedProductId) {
        Set<String> referenceTokens = extractNameTokens(productName);
        if (referenceTokens.isEmpty()) {
            return List.of();
        }

        return repository.findAll().stream()
                .filter(product -> product.category.equalsIgnoreCase(category))
                .filter(product -> excludedProductId == null || !product.id.equals(excludedProductId))
                .map(product -> new AbstractMap.SimpleEntry<>(
                        product,
                        similarityScore(referenceTokens, extractNameTokens(product.name))))
                .filter(entry -> entry.getValue() > 0)
                .sorted(this::compareBySimilarityThenRating)
                .limit(MAX_SIMILAR_PRODUCTS)
                .map(entry -> toDTO(entry.getKey()))
                .toList();
    }

    public List<ProductDTO> searchByKeyword(String keyword) {
        String normalizedKeyword = keyword.toLowerCase();

        return repository.findAll().stream()
                .filter(product -> product.name.toLowerCase().contains(normalizedKeyword))
                .map(this::toDTO)
                .toList();
    }

    public Map<String, List<ProductDTO>> getPriceRanges() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(this::getPriceRangeLabel));
    }

    private String getPriceRangeLabel(ProductDTO product) {
        if (product.getPrice() < 20) {
            return "0-20€ (Economici)";
        }

        if (product.getPrice() < 50) {
            return "20-50€ (Medi)";
        }

        return "50€+ (Costosi)";
    }

    public Product getProductById(Long productId) {
        return repository.findById(productId);
    }

    public List<ProductDTO> getAllProductsOrderedById() {
        return repository.findAll().stream()
                .sorted(Comparator.comparingLong(product -> product.id))
                .map(this::toDTO)
                .toList();
    }

    public void addReview(Long productId, String review) {
        Product product = getProductById(productId);
        if (product == null) {
            return;
        }

        product.reviews.add(review);
    }

    public void updatePrice(Long productId, double newPrice) {
        Product product = getProductById(productId);
        if (product == null) {
            return;
        }

        product.price = newPrice;
    }

    // Metodi per ottenere una risorsa singola
    // Estrarre le recensioni di un prodotto
    public List<String> getProductReviews(Long productId) {
        Product product = getProductById(productId);
        if (product == null) {
            return List.of();
        }

        return product.reviews;
    }

    // Estrarre il prezzo di un prodotto
    public double getProductPrice(Long productId) {
        Product product = getProductById(productId);
        if (product == null) {
            return 0.0;
        }

        return product.price;
    }

    private int compareBySimilarityThenRating(
            AbstractMap.SimpleEntry<Product, Integer> firstEntry,
            AbstractMap.SimpleEntry<Product, Integer> secondEntry) {
        // Prima vengono i prodotti con piu' parole in comune; a parita' vince il rating.
        int byScore = Integer.compare(secondEntry.getValue(), firstEntry.getValue());
        if (byScore != 0) {
            return byScore;
        }

        return Double.compare(secondEntry.getKey().rating, firstEntry.getKey().rating);
    }

    private Set<String> extractNameTokens(String name) {
        if (name == null) {
            return Set.of();
        }

        // Normalizza il nome in parole confrontabili, scartando parole troppo generiche.
        return Arrays.stream(name.toLowerCase().split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 3)
                .collect(Collectors.toSet());
    }

    private int similarityScore(Set<String> referenceTokens, Set<String> candidateTokens) {
        int score = 0;
        for (String token : candidateTokens) {
            if (referenceTokens.contains(token)) {
                score++;
            }
        }

        return score;
    }
}
