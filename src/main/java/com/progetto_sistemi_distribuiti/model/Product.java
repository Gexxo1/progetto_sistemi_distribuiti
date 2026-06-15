package com.progetto_sistemi_distribuiti.model;

import java.util.ArrayList;
import java.util.List;

// Modello Dati Interno (Entità)
public class Product {
    public Long id;
    public String name;
    public String category;
    public double price;
    public double rating;
    public List<String> reviews = new ArrayList<>();

    public Product(Long id, String name, String category, double price, double rating) {
        this.id = id; this.name = name; this.category = category; this.price = price; this.rating = rating;
    }
}
