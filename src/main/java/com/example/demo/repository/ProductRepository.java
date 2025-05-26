package com.example.demo.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.demo.model.Product;

/**
 * 產品的數據倉庫。
 * 此範例使用記憶體內存儲 (In-memory ConcurrentHashMap) 來模擬數據庫。
 */
@Repository
public class ProductRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public void save(Product product) {
        products.put(product.getId(), product);
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public Collection<Product> findAll() {
        return products.values();
    }
}