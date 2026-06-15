package com.progetto_sistemi_distribuiti.repository;

import com.progetto_sistemi_distribuiti.model.Product;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepository {
    private static final int MAX_IMPORTED_REVIEWS_PER_PRODUCT = 3;
    private static final int MIN_REVIEW_LENGTH = 20;
    private static final int MAX_REVIEW_LENGTH = 250;

    private final List<Product> database = new ArrayList<>();
    private Long idCounter = 1L; // Ci serve per generare ID univoci per tutti i prodotti

    @PostConstruct
    public void init() {
        // carichiamo tutti e tre i dataset
        loadDataset("products.csv", "Skincare");
        loadDataset("products2.csv", "Skincare");
        loadDataset("fashion.csv", "Fashion");
        loadDataset("laptops.csv", "Laptops");

        System.out.println("Caricamento completato! Prodotti totali in memoria: " + database.size());

        /* Placeholder se senza CSV
        database.add(new Product(1L, "Crema Viso Idratante", "Skincare", 10.50, 4.8));
        database.add(new Product(2L, "Siero Anti-Eta", "Skincare", 5.00, 4.9));
        database.add(new Product(3L, "Detergente Viso", "Skincare", 120.00, 4.2));
        database.add(new Product(4L, "Giacca di Pelle", "Fashion", 100.00, 4.5));
        database.add(new Product(5L, "T-Shirt Bianca", "Fashion", 15.00, 4.0));

        database.get(0).reviews.add("Ottimo prodotto");
        */
    }

    private void loadDataset(String fileName, String category) {
        InputStream inputStream = getClass().getResourceAsStream("/" + fileName);
        if (inputStream == null) {
            System.out.println("File non trovato: " + fileName);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setAllowMissingColumnNames(true) // ignora la colonna fantasma
                    .build()
                    .parse(reader);

            for (CSVRecord record : records) {
                addProductFromRecord(record, category);
            }
        } catch (Exception e) {
            System.err.println("Errore fatale nella lettura del file " + fileName + ": " + e.getMessage());
        }
    }

    private void addProductFromRecord(CSVRecord record, String category) {
        try {
            Product product = buildProduct(record, category);
            addDatasetReviews(product, record, category);
            database.add(product);
        } catch (Exception e) {
            // Ignora le singole righe corrotte
        }
    }

    private Product buildProduct(CSVRecord record, String category) {
        // Andiamo a controllare l'input in base alle diverse formattazioni dei file
        return switch (category) {
            case "Skincare" -> buildSkincareProduct(record, category);
            case "Fashion" -> buildFashionProduct(record, category);
            case "Laptops" -> buildLaptopProduct(record, category);
            default -> new Product(idCounter++, "", category, 0.0, 0.0);
        };
    }

    private Product buildSkincareProduct(CSVRecord record, String category) {
        String name = record.get("product_name");
        double price = parseDoubleOrZero(record.get("price_usd"));
        double rating = parseDoubleOrZero(record.get("rating"));

        return new Product(idCounter++, name, category, price, rating);
    }

    private Product buildFashionProduct(CSVRecord record, String category) {
        // Correggiamo il nome della colonna del prodotto
        String name = record.get("product_name");

        // Il prezzo si trova sotto "price" (la funzione parseDoubleOrZero rimuovera in automatico il simbolo della valuta)
        double price = parseDoubleOrZero(record.get("price"));

        // Estraiamo solo il primo numero dalla frase "4.9 out of 5 stars"
        String rawRating = record.get("average_review_rating");
        if (rawRating != null && rawRating.contains(" out of ")) {
            rawRating = rawRating.split(" ")[0]; // Prende solo il "4.9" prima dello spazio
        }
        double rating = parseDoubleOrZero(rawRating);

        return new Product(idCounter++, name, category, price, rating);
    }

    private Product buildLaptopProduct(CSVRecord record, String category) {
        String name = record.get("Company") + " " + record.get("Product");
        double price = parseDoubleOrZero(record.get("Price_euros"));
        double rating = 4.5;

        return new Product(idCounter++, name, category, price, rating);
    }

    // Utility per evitare crash se il prezzo e vuoto o ha il simbolo dell'euro nel CSV
    private double parseDoubleOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Rimuove eventuali simboli (es. euro, $, sterlina, spazi) e sostituisce la virgola col punto
            String cleaned = value.replaceAll("[^\\d.,]", "").replace(",", ".");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void addDatasetReviews(Product product, CSVRecord record, String category) {
        switch (category) {
            case "Fashion" -> addReviewSnippets(product, getOptional(record, "customer_reviews"));
            case "Skincare" -> addSkincareReviewSummary(product, record);
            case "Laptops" -> addLaptopTechnicalSummary(product, record);
        }
    }

    private void addSkincareReviewSummary(Product product, CSVRecord record) {
        String reviews = getOptional(record, "reviews");
        if (!reviews.isBlank()) {
            product.reviews.add("Rating medio " + product.rating + " basato su " + reviews + " recensioni nel dataset.");
        }
    }

    private void addLaptopTechnicalSummary(Product product, CSVRecord record) {
        String cpu = getOptional(record, "Cpu");
        String ram = getOptional(record, "Ram");
        String memory = getOptional(record, "Memory");

        // Nei laptop non ci sono review testuali: usiamo una sintesi tecnica come contenuto informativo.
        String details = String.join(", ", List.of(cpu, ram, memory)).replaceAll("(^,\\s*)|(,\\s*$)", "");
        if (!details.isBlank()) {
            product.reviews.add("Scheda tecnica dal dataset: " + details + ".");
        }
    }

    private void addReviewSnippets(Product product, String rawReviews) {
        if (rawReviews == null || rawReviews.isBlank()) {
            return;
        }

        String cleaned = rawReviews
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Alcuni CSV mettono piu' recensioni nella stessa cella, separate da // oppure |.
        String[] snippets = cleaned.split("\\s+//\\s+|\\s+\\|\\s+");
        for (String snippet : snippets) {
            addReviewSnippet(product, snippet);
            if (product.reviews.size() == MAX_IMPORTED_REVIEWS_PER_PRODUCT) {
                return;
            }
        }
    }

    private void addReviewSnippet(Product product, String snippet) {
        String review = snippet.trim();
        if (review.length() < MIN_REVIEW_LENGTH) {
            return;
        }

        if (review.length() > MAX_REVIEW_LENGTH) {
            review = review.substring(0, MAX_REVIEW_LENGTH) + "...";
        }

        product.reviews.add(review);
    }

    private String getOptional(CSVRecord record, String column) {
        try {
            if (record.isMapped(column) && record.isSet(column)) {
                String value = record.get(column);
                if (value == null) {
                    return "";
                }

                return value.trim();
            }
        } catch (IllegalArgumentException e) {
            return "";
        }

        return "";
    }

    public List<Product> findAll() {
        return database;
    }

    public Product findById(Long id) {
        return database.stream()
                .filter(product -> product.id.equals(id))
                .findFirst()
                .orElse(null);
    }
}
