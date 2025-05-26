package com.example.demo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 代表一張固定金額折抵的優惠券。
 * 包含優惠券代碼、描述和折抵金額。
 */
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String description;
    private final Integer discountAmount; // 折抵金額

    /**
     * 建構一個新的固定金額優惠券。
     * @param code 優惠券的唯一代碼
     * @param description 優惠券的描述文字
     * @param discountAmount 優惠券的固定折抵金額
     */
    public Coupon(String code, String description, Integer discountAmount) {
        this.code = code;
        this.description = description;
        this.discountAmount = discountAmount;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public Integer getDiscountAmount() { return discountAmount; } // Getter 名稱更改

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(code, coupon.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}