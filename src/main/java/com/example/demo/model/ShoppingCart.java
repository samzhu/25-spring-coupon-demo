package com.example.demo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 代表一個完整的購物車狀態模型，包含購物車中的商品項目、已套用的優惠券、原始總價及折扣後總價。
 * <p>
 * 此模型主要用於封裝和表示購物車的完整數據結構。它可以：
 * <ul>
 *   <li>作為從服務層獲取完整購物車計算結果後，用於填充視圖（尤其是在伺服器端渲染）的資料容器。</li>
 *   <li>在需要將整個購物車狀態持久化或在不同層之間傳遞完整購物車資訊時使用。</li>
 * </ul>
 * <p>
 * 注意：在當前的 RESTful API 設計中 (例如 {@code CartController} 和 {@code CartService})，
 * 購物車的計算和數據傳輸更多依賴於特定的資料傳輸物件 (DTOs) 如 {@code ShoppingCartInput} 和 {@code CalculationResultDto}。
 * 此 {@code ShoppingCart} 模型則更側重於表示一個全面的、可操作的購物車實體。
 * </p>
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

    /**
     * 將商品添加到購物車。
     * <p>
     * 如果商品已存在於購物車中，則增加其數量；否則，將新商品添加到購物車。
     * 如果傳入的 {@code item} 或其內部 {@code product} 為 {@code null}，則此操作無效。
     * </p>
     * @param item 要添加的購物車項目。
     */
    public void addItem(CartItem item) {
        // 防禦：檢查商品及商品內容是否有效
        if (item == null || item.getProduct() == null) {
            return;
        }
        for (CartItem existingItem : items) {
            if (existingItem.getProduct().getId().equals(item.getProduct().getId())) {
                // 商品已存在，更新數量
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        // 新商品，加入列表
        items.add(item);
    }

    /**
     * 添加一張已套用的優惠券到列表。
     * @param coupon 要添加的優惠券。
     */
    public void addAppliedCoupon(Coupon coupon) {
        // 檢查優惠券是否有效，且尚未套用
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

    /**
     * 清空購物車中的所有商品、已套用的優惠券，並將原始總價與折扣後總價重設為零。
     */
    public void clearItemsAndCoupons() {
        items.clear();
        clearAppliedCoupons(); // 複用清除優惠券的邏輯
        originalTotal = BigDecimal.ZERO;
        discountedTotal = BigDecimal.ZERO;
    }

    /**
     * 檢查購物車是否有有效的折扣被套用。
     * <p>
     * 當原始總價大於折扣後總價時，視為有折扣被套用。
     * 如果原始總價或折扣後總價為 {@code null}，則視為無折扣。
     * </p>
     * @return 如果有折扣被套用則返回 {@code true}，否則返回 {@code false}。
     */
    public boolean isDiscountApplied() {
        // 總價必須存在才能判斷折扣
        if (originalTotal == null || discountedTotal == null) {
            return false;
        }
        // 原始總價大於折扣後總價表示有折扣
        return originalTotal.compareTo(discountedTotal) > 0;
    }

    /**
     * 計算購物車中已套用折扣的總金額。
     * <p>
     * 折扣金額為原始總價減去折扣後總價。
     * 如果計算出的折扣小於或等於零，或者原始總價/折扣後總價為 {@code null}，則返回 {@link BigDecimal#ZERO}。
     * </p>
     * @return 套用的總折扣金額；如果無折扣，則返回 {@link BigDecimal#ZERO}。
     */
    public BigDecimal getTotalDiscountAmount() {
        // 總價必須存在才能計算折扣
        if (originalTotal == null || discountedTotal == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = originalTotal.subtract(discountedTotal);
        // 確保返回的折扣金額為正數，若無折扣或計算錯誤則返回零
        return discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ZERO;
    }
}