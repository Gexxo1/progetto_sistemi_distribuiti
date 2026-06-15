package com.progetto_sistemi_distribuiti.model;

// DTO = Data Transfer Object --> serve a trasferire i dati di Product
// CONSEGNA #1.3: Usare il design pattern DTO per organizzare lo scambio di dati fra le parti client e server
public class ProductDTO {
    
    private Long id;
    private String name;
    private String category;
    private double price;
    private double rating;

    // Costruttore vuoto (necessario a Spring per la serializzazione JSON)
    public ProductDTO() {
    }

    // Costruttore con parametri (usato nel ProductService)
    public ProductDTO(Long id, String name, String category, double price, double rating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.rating = rating;
    }

    // --- GETTER E SETTER ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
