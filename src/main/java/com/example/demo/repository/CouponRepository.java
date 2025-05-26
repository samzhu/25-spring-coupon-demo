package com.example.demo.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.demo.model.Coupon;

/**
 * 優惠券的數據倉庫。
 * 此範例使用記憶體內存儲 (In-memory ConcurrentHashMap) 來模擬數據庫。
 */
@Repository
public class CouponRepository {
    private final Map<String, Coupon> coupons = new ConcurrentHashMap<>();

    public void save(Coupon coupon) {
        coupons.put(coupon.getCode(), coupon);
    }

    public Optional<Coupon> findByCode(String code) {
        return Optional.ofNullable(coupons.get(code));
    }

    public Collection<Coupon> findAll() {
        return coupons.values();
    }
}