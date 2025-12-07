package com.kosi.financetracker.backend.Controller;

import com.kosi.financetracker.backend.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@CrossOrigin(origins = "http://localhost:3000")
public class CurrencyController {

    @Autowired
    private CurrencyConversionService currencyService;

    /**
     * Get exchange rate between two currencies
     */
    @GetMapping("/rate")
    public ResponseEntity<?> getExchangeRate(
            @RequestParam String from,
            @RequestParam String to) {
        try {
            double rate = currencyService.convert(1.0, from, to);
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", from.toUpperCase());
            response.put("to", to.toUpperCase());
            response.put("rate", rate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get exchange rate: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Convert an amount from one currency to another
     */
    @GetMapping("/convert")
    public ResponseEntity<?> convertAmount(
            @RequestParam double amount,
            @RequestParam String from,
            @RequestParam String to) {
        try {
            double converted = currencyService.convert(amount, from, to);
            
            Map<String, Object> response = new HashMap<>();
            response.put("originalAmount", amount);
            response.put("fromCurrency", from.toUpperCase());
            response.put("toCurrency", to.toUpperCase());
            response.put("convertedAmount", converted);
            response.put("fromSymbol", currencyService.getCurrencySymbol(from));
            response.put("toSymbol", currencyService.getCurrencySymbol(to));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to convert amount: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all available exchange rates
     */
    @GetMapping("/rates")
    public ResponseEntity<?> getAllRates() {
        try {
            Map<String, Double> rates = currencyService.getAllRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get rates: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get currency symbol for a currency code
     */
    @GetMapping("/symbol/{currencyCode}")
    public ResponseEntity<?> getCurrencySymbol(@PathVariable String currencyCode) {
        try {
            String symbol = currencyService.getCurrencySymbol(currencyCode);
            
            Map<String, String> response = new HashMap<>();
            response.put("currency", currencyCode.toUpperCase());
            response.put("symbol", symbol);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get symbol: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}