package com.kosi.financetracker.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Cache exchange rates (in production, you'd want to refresh these periodically)
    private Map<String, Double> exchangeRates = new HashMap<>();
    private long lastUpdated = 0;
    private static final long CACHE_DURATION = 3600000; // 1 hour in milliseconds

    /**
     * Get current exchange rate from USD to target currency
     */
    public double getExchangeRate(String targetCurrency) {
        // Update cache if needed
        if (System.currentTimeMillis() - lastUpdated > CACHE_DURATION || exchangeRates.isEmpty()) {
            updateExchangeRates();
        }
        
        return exchangeRates.getOrDefault(targetCurrency.toUpperCase(), 1.0);
    }

    /**
     * Convert amount from one currency to another
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        // Update rates if needed
        if (System.currentTimeMillis() - lastUpdated > CACHE_DURATION || exchangeRates.isEmpty()) {
            updateExchangeRates();
        }

        // Convert from source currency to USD, then to target currency
        double toUsd = amount / exchangeRates.getOrDefault(fromCurrency.toUpperCase(), 1.0);
        double toTarget = toUsd * exchangeRates.getOrDefault(toCurrency.toUpperCase(), 1.0);
        
        return Math.round(toTarget * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Fetch latest exchange rates from API
     */
    private void updateExchangeRates() {
        try {
            String response = restTemplate.getForObject(API_URL, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode rates = root.get("rates");
            
            // Clear old rates
            exchangeRates.clear();
            
            // Add USD as base
            exchangeRates.put("USD", 1.0);
            
            // Add all other rates
            rates.fields().forEachRemaining(entry -> {
                exchangeRates.put(entry.getKey(), entry.getValue().asDouble());
            });
            
            lastUpdated = System.currentTimeMillis();
            System.out.println("Exchange rates updated successfully. Total currencies: " + exchangeRates.size());
            
        } catch (Exception e) {
            System.err.println("Failed to update exchange rates: " + e.getMessage());
            
            // Fallback to common rates if API fails
            if (exchangeRates.isEmpty()) {
                exchangeRates.put("USD", 1.0);
                exchangeRates.put("EUR", 0.92);
                exchangeRates.put("GBP", 0.79);
                exchangeRates.put("JPY", 149.50);
                exchangeRates.put("CAD", 1.36);
                exchangeRates.put("AUD", 1.52);
            }
        }
    }

    /**
     * Get currency symbol for a currency code
     */
    public String getCurrencySymbol(String currencyCode) {
        Map<String, String> symbols = new HashMap<>();
        symbols.put("USD", "$");
        symbols.put("EUR", "€");
        symbols.put("GBP", "£");
        symbols.put("JPY", "¥");
        symbols.put("CAD", "C$");
        symbols.put("AUD", "A$");
        symbols.put("CHF", "Fr");
        symbols.put("CNY", "¥");
        symbols.put("INR", "₹");
        
        return symbols.getOrDefault(currencyCode.toUpperCase(), currencyCode);
    }

    /**
     * Get all available currencies
     */
    public Map<String, Double> getAllRates() {
        if (System.currentTimeMillis() - lastUpdated > CACHE_DURATION || exchangeRates.isEmpty()) {
            updateExchangeRates();
        }
        return new HashMap<>(exchangeRates);
    }
}