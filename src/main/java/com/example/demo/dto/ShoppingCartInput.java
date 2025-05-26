package com.example.demo.dto;

import java.util.List;

/**
 * 用於向服務傳遞整個購物車內容以進行計算的 DTO。
 * 
 * @param items       購物車中的商品項目列表 (使用 CartItemInput DTO)
 * @param couponCodes 已選擇套用的優惠券代碼列表
 */
public record ShoppingCartInput(List<CartItemInput> items, List<String> couponCodes) {
}
