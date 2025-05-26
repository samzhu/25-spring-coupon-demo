package com.example.demo.model;

import java.math.BigDecimal;

/**
 * 代表商店中的一個產品。
 * 包含產品的基本資訊如 ID、名稱和價格。
 */
public class Product {
    private final String id;
    private final String name;
    private final BigDecimal price;

    /**
     * 建構一個新的產品。
     * 
     * @param id    產品的唯一識別碼
     * @param name  產品的名稱
     * @param price 產品的價格
     */
    public Product(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /**
     * 獲取產品 ID。
     * 
     * @return 產品 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 獲取產品名稱。
     * 
     * @return 產品名稱
     */
    public String getName() {
        return name;
    }

    /**
     * 獲取產品價格。
     * 
     * @return 產品價格
     */
    public BigDecimal getPrice() {
        return price;
    }

}
