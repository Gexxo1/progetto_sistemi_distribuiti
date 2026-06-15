package com.progetto_sistemi_distribuiti.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import java.util.ArrayList;
import java.util.List;

// CONSEGNA #1.2: Usare uno dei design pattern Session State
@Component
@SessionScope
public class UserSession {
    private final List<String> searchHistory = new ArrayList<>();

    public void addSearchKeyword(String keyword) {
        // Aggiunge la parola cercata alla cronologia, evitando doppioni consecutivi
        if (!isRepeatedLastSearch(keyword)) {
            searchHistory.add(keyword);
        }
    }

    public List<String> getSearchHistory() {
        return searchHistory;
    }

    private boolean isRepeatedLastSearch(String keyword) {
        if (searchHistory.isEmpty()) {
            return false;
        }

        String lastKeyword = searchHistory.get(searchHistory.size() - 1);
        return lastKeyword.equals(keyword);
    }
}
