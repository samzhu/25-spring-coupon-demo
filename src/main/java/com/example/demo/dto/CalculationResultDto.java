package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.model.Coupon;

/**
 * DTO 用於封裝購物車計算結果。
 * 
 * @param originalTotal       原始總價
 * @param discountedTotal     折扣後總價
 * @param totalDiscountAmount 總折扣金額
 * @param appliedCoupon       如果有優惠券被套用，則為該優惠券的詳細信息；否則為 null。
 */
public record CalculationResultDto(
                BigDecimal originalTotal,
                BigDecimal discountedTotal,
                BigDecimal totalDiscountAmount,
                List<Coupon> appliedCoupons) {

}
