package com.example.demo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 代表一個用於視圖展示的購物車。
 * Controller 會用 CartService 計算的結果來填充此物件的總價等信息。
 * 或者，在完全由JS驅動的視圖中，此物件可能僅作為後端概念模型。
 */
public class ShoppingCart {

    private final List<CartItem> items = new ArrayList<>();
    private final List<Coupon> appliedCoupons = new ArrayList<>();
    private BigDecimal originalTotal = BigDecimal.ZERO;
    private BigDecimal discountedTotal = BigDecimal.ZERO;

    public List<CartItem> getItems() { return items; }
    public List<Coupon> getAppliedCoupons() { return appliedCoupons; }

    public BigDecimal getOriginalTotal() { return originalTotal; }
    public void setOriginalTotal(BigDecimal originalTotal) { this.originalTotal = originalTotal; }
    public BigDecimal getDiscountedTotal() { return discountedTotal; }
    public void setDiscountedTotal(BigDecimal discountedTotal) { this.discountedTotal = discountedTotal; }

    public void addItem(CartItem item) {
        if (item == null || item.getProduct() == null) return;
        for (CartItem existingItem : items) {
            if (existingItem.getProduct().getId().equals(item.getProduct().getId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    /**
     * 添加一張已套用的優惠券到列表。
     * @param coupon 要添加的優惠券。
     */
    public void addAppliedCoupon(Coupon coupon) {
        if (coupon != null && !this.appliedCoupons.contains(coupon)) {
            this.appliedCoupons.add(coupon);
        }
    }
    
    /**
     * 清除所有已套用的優惠券。
     */
    public void clearAppliedCoupons() {
        this.appliedCoupons.clear();
    }

    public void clearItemsAndCoupons() {
        items.clear();
        clearAppliedCoupons();
        originalTotal = BigDecimal.ZERO;
        discountedTotal = BigDecimal.ZERO;
    }

    public boolean isDiscountApplied() {
        if (originalTotal == null || discountedTotal == null) return false;
        return originalTotal.compareTo(discountedTotal) > 0;
    }

    public BigDecimal getTotalDiscountAmount() {
        if (originalTotal == null || discountedTotal == null) return BigDecimal.ZERO;
        BigDecimal discount = originalTotal.subtract(discountedTotal);
        return discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ZERO;
    }
}