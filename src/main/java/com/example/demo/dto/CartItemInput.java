package com.example.demo.dto;

/**
 * 用於向服務傳遞購物車項目信息的 DTO。
 * 只包含產品 ID 和數量。
 * 
 * @param productId 產品的唯一識別碼
 * @param quantity  購買數量
 */
public record CartItemInput(String productId, int quantity) {

}
