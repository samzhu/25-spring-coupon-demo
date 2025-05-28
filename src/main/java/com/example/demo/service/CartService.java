package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CalculationResultDto;
import com.example.demo.exception.TotalDiscountExceededException;
import com.example.demo.dto.CartItemInput;
import com.example.demo.dto.ShoppingCartInput;
import com.example.demo.model.Coupon;
import com.example.demo.model.Product;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.ProductRepository;

/**
 * 購物車服務負責根據輸入的購物車資料計算總價和折扣。
 */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public CartService(ProductRepository productRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    /**
     * 根據輸入的 {@link ShoppingCartInput} 計算購物車的總價和折扣。
     * <p>
     * 此方法會將所有提供的有效固定金額優惠券的折扣疊加。
     *
     * @param cartInput 包含購物車項目、數量和優惠券代碼列表的輸入物件。
     * @return {@link CalculationResultDto} 包含原始總價、折扣後總價、實際折扣金額以及所有套用的優惠券列表。
     */
    public CalculationResultDto calculateCartPrice(ShoppingCartInput cartInput) {
        Integer rawTotalPrice = calculateRawTotalPrice(cartInput.items());

        List<Coupon> appliedCoupons = new ArrayList<>();
        Integer totalDiscountAmountFromCoupons = applyCoupons(cartInput.couponCodes(), appliedCoupons, rawTotalPrice);

        // 計算折扣後總價
        Integer finalPrice = rawTotalPrice - totalDiscountAmountFromCoupons;

        Integer effectiveTotalDiscount = rawTotalPrice - finalPrice;

        log.info("計算完成。原始總價: {}, 折扣後總價: {}, 優惠券聲稱總折扣: {}, 實際總折扣: {}",
                rawTotalPrice, finalPrice, totalDiscountAmountFromCoupons, effectiveTotalDiscount);

        return new CalculationResultDto(rawTotalPrice, finalPrice, effectiveTotalDiscount, appliedCoupons);
    }

    /**
     * 計算購物車中所有商品的原始總價。
     *
     * @param items 購物車中的商品項目列表。
     * @return 原始總價。
     */
    private Integer calculateRawTotalPrice(List<CartItemInput> items) {
        Integer currentRawTotalPrice = 0;
        if (items != null) {
            for (CartItemInput itemInput : items) {
                Optional<Product> optionalProduct = productRepository.findById(itemInput.productId());
                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();
                    currentRawTotalPrice += product.getPrice() * itemInput.quantity();
                } else {
                    log.warn("計算時找不到產品 ID: {}。此商品將不列入計算。", itemInput.productId());
                }
            }
        }
        return currentRawTotalPrice;
    }

    /**
     * 處理並套用提供的優惠券代碼。
     *
     * @param couponCodes    使用者提供的優惠券代碼列表。
     * @param appliedCoupons 用於收集實際套用的優惠券實例列表 (此列表會被此方法修改)。
     * @param rawTotalPrice  購物車的原始總價。
     * @return 從所有套用的優惠券中獲得的總折扣金額。
     * @throws TotalDiscountExceededException 如果套用優惠券後的總折扣金額超過原始總價。
     */
    private Integer applyCoupons(List<String> couponCodes, List<Coupon> appliedCoupons, Integer rawTotalPrice) {
        Integer currentTotalDiscount = 0;
        if (couponCodes != null && !couponCodes.isEmpty()) {
            for (String couponCode : couponCodes) {
                if (couponCode != null && !couponCode.trim().isEmpty()) {
                    Optional<Coupon> optionalCoupon = couponRepository.findByCode(couponCode);
                    if (optionalCoupon.isPresent()) {
                        Coupon coupon = optionalCoupon.get();
                        if (currentTotalDiscount + coupon.getDiscountAmount() > rawTotalPrice) {
                            throw new TotalDiscountExceededException("所選優惠券總折價已達上限，無法套用更多優惠券");
                        }
                        appliedCoupons.add(coupon);
                        currentTotalDiscount += coupon.getDiscountAmount();
                        log.debug("套用優惠券 '{}', 折抵金額: {}", coupon.getDescription(), coupon.getDiscountAmount());
                    } else {
                        log.warn("計算時找不到優惠券代碼: {}。此券將不被套用。", couponCode);
                    }
                }
            }
        }
        return currentTotalDiscount;
    }
}