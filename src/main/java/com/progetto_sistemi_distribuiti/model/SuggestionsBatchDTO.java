package com.progetto_sistemi_distribuiti.model;

import java.util.List;


// CONSEGNA #1.4: Usare il design pattern Request Batch per dare all’utente suggerimenti (per scoprire dati utili)
public class SuggestionsBatchDTO {

    private List<ProductDTO> topRated;
    private List<ProductDTO> bestPrices;
    private List<ProductDTO> similar;

    // Costruttore vuoto (necessario a Spring per la conversione in JSON)
    public SuggestionsBatchDTO() {
    }

    // Costruttore con parametri (usato nel ProductFacadeController)
    public SuggestionsBatchDTO(List<ProductDTO> topRated, List<ProductDTO> bestPrices, List<ProductDTO> similar) {
        this.topRated = topRated;
        this.bestPrices = bestPrices;
        this.similar = similar;
    }

    // --- GETTER E SETTER ---

    public List<ProductDTO> getTopRated() {
        return topRated;
    }

    public void setTopRated(List<ProductDTO> topRated) {
        this.topRated = topRated;
    }

    public List<ProductDTO> getBestPrices() {
        return bestPrices;
    }

    public void setBestPrices(List<ProductDTO> bestPrices) {
        this.bestPrices = bestPrices;
    }

    public List<ProductDTO> getSimilar() {
        return similar;
    }

    public void setSimilar(List<ProductDTO> similar) {
        this.similar = similar;
    }
}