package com.example.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CalculationResultDto;
import com.example.demo.dto.CartItemInput;
import com.example.demo.dto.ShoppingCartInput;
import com.example.demo.exception.ExcessiveDiscountException;
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
     * 此方法將選擇提供的有效固定金額優惠券中折扣最大的一個進行套用。
     * 如果最佳優惠券的折扣金額超過原始總價，將拋出 {@link ExcessiveDiscountException}。
     *
     * @param cartInput 包含購物車項目、數量和優惠券代碼列表的輸入物件。
     * @return {@link CalculationResultDto} 包含原始總價、折扣後總價、實際折扣金額以及套用的最佳優惠券（如果有的話）。
     * @throws ExcessiveDiscountException 如果選擇的最佳優惠券折扣金額大於購物車原始總價。
     */
    public CalculationResultDto calculateCartPrice(ShoppingCartInput cartInput) {
        Integer rawTotalPrice = calculateRawTotalPrice(cartInput.items());

        Coupon bestCoupon = findBestCoupon(cartInput.couponCodes());
        Integer totalDiscountAmountFromCoupon = 0;
        List<Coupon> appliedCouponsList = new ArrayList<>();

        if (bestCoupon != null) {
            if (bestCoupon.getDiscountAmount() > rawTotalPrice) {
                log.warn("所選優惠券 '{}' 折扣金額 {} 超過原始總價 {}。拋出 ExcessiveDiscountException。",
                        bestCoupon.getCode(), bestCoupon.getDiscountAmount(), rawTotalPrice);
                throw new ExcessiveDiscountException("所選優惠券總折價已達上限，無法套用更多優惠券");
            }
            totalDiscountAmountFromCoupon = bestCoupon.getDiscountAmount();
            appliedCouponsList.add(bestCoupon);
            log.info("套用最佳優惠券: {} (代碼: {}), 折扣金額: {}", bestCoupon.getDescription(), bestCoupon.getCode(), bestCoupon.getDiscountAmount());
        } else {
            log.info("沒有有效的優惠券可供套用。");
        }

        // 計算折扣後總價
        Integer finalPrice = rawTotalPrice - totalDiscountAmountFromCoupon;

        // 實際折扣等於優惠券折扣，因為我們只套用一個（或零個）
        Integer effectiveTotalDiscount = totalDiscountAmountFromCoupon;

        log.info("計算完成。原始總價: {}, 折扣後總價: {}, 優惠券折扣金額: {}, 實際總折扣: {}",
                rawTotalPrice, finalPrice, totalDiscountAmountFromCoupon, effectiveTotalDiscount);

        return new CalculationResultDto(rawTotalPrice, finalPrice, effectiveTotalDiscount, appliedCouponsList);
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
     * 從提供的優惠券代碼列表中找出折扣金額最高的有效優惠券。
     *
     * @param couponCodes 使用者提供的優惠券代碼列表。
     * @return 折扣金額最高的 {@link Coupon}，如果沒有有效的優惠券則返回 {@code null}。
     */
    private Coupon findBestCoupon(List<String> couponCodes) {
        if (couponCodes == null || couponCodes.isEmpty()) {
            return null;
        }

        Coupon bestCoupon = null;
        for (String couponCode : couponCodes) {
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Optional<Coupon> optionalCoupon = couponRepository.findByCode(couponCode);
                if (optionalCoupon.isPresent()) {
                    Coupon currentCoupon = optionalCoupon.get();
                    if (bestCoupon == null || currentCoupon.getDiscountAmount() > bestCoupon.getDiscountAmount()) {
                        bestCoupon = currentCoupon;
                    }
                    log.debug("找到有效優惠券 '{}' (代碼: {}), 折扣金額: {}", currentCoupon.getDescription(), currentCoupon.getCode(), currentCoupon.getDiscountAmount());
                } else {
                    log.warn("找不到優惠券代碼: {}。此券將不被考慮。", couponCode);
                }
            }
        }

        if (bestCoupon != null) {
            log.info("選擇的最佳優惠券: {} (代碼: {}), 折扣金額: {}", bestCoupon.getDescription(), bestCoupon.getCode(), bestCoupon.getDiscountAmount());
        } else {
            log.info("提供的優惠券代碼中沒有找到有效的優惠券。");
        }
        return bestCoupon;
    }
}