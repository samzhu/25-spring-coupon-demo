package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CalculationResultDto;
import com.example.demo.dto.CartItemInput;
import com.example.demo.dto.ShoppingCartInput;
import com.example.demo.model.Coupon;
import com.example.demo.model.Product;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.ProductRepository;

/**
 * 無狀態的購物車服務類別。
 * 根據輸入的 DTO 計算購物車總價，並返回一個結果 DTO。
 * 此版本將錯誤地疊加所有傳入的有效優惠券。
 */
@Service
public class CartService {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public CartService(ProductRepository productRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        System.out.println("Stateless CartService (Pure DTO Calculation, Multiple Coupons Flawed) 已初始化。");
    }

    /**
     * 根據輸入的 ShoppingCartInput DTO 計算總價和折扣。
     * 此方法會錯誤地疊加所有有效的固定金額優惠券。
     *
     * @param cartInputDto 包含購物車項目ID、數量和優惠券代碼列表的 DTO。
     * @return CalculationResultDto 包含原始總價、折扣後總價、折扣金額和所有套用的優惠券列表。
     */
    public CalculationResultDto calculateTotalsFromDto(ShoppingCartInput cartInputDto) {
        Integer originalTotal = 0;
        List<Coupon> appliedCouponInstances = new ArrayList<>();
        Integer totalDiscountFromCoupons = 0;

        // 1. 計算原始總價
        if (cartInputDto.items() != null) {
            for (CartItemInput itemInput : cartInputDto.items()) {
                Optional<Product> productOpt = productRepository.findById(itemInput.productId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    originalTotal = originalTotal + (product.getPrice() * itemInput.quantity());
                } else {
                    System.err.println("警告：計算時找不到產品 ID: " + itemInput.productId());
                }
            }
        }
        

        // 2. 處理多張優惠券 (錯誤疊加邏輯)
        if (cartInputDto.couponCodes() != null && !cartInputDto.couponCodes().isEmpty()) {
            for (String couponCode : cartInputDto.couponCodes()) {
                if (couponCode != null && !couponCode.trim().isEmpty()) {
                    Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode);
                    if (couponOpt.isPresent()) {
                        Coupon coupon = couponOpt.get();
                        appliedCouponInstances.add(coupon);
                        totalDiscountFromCoupons = totalDiscountFromCoupons + coupon.getDiscountAmount();
                        System.out.println("DEBUG (CartService - Flawed Multiple): 套用優惠券 '" + coupon.getDescription()
                                + "', 折抵金額: " + coupon.getDiscountAmount());
                    } else {
                        System.err.println("警告：計算時找不到優惠券代碼: " + couponCode + "。此券將不被套用。");
                    }
                }
            }
        }

        // 3. 計算折扣後總價
        Integer discountedTotal = originalTotal - totalDiscountFromCoupons;
        if (discountedTotal < 0) {
            discountedTotal = 0;
        }

        Integer actualTotalDiscountApplied = originalTotal - discountedTotal;

        System.out.println("DEBUG (CartService - Flawed Multiple): 計算完成。原始總價: " + originalTotal +
                ", 折扣後總價: " + discountedTotal +
                ", 優惠券聲稱總折扣: " + totalDiscountFromCoupons +
                ", 實際總折扣: " + actualTotalDiscountApplied);

        return new CalculationResultDto(originalTotal, discountedTotal, actualTotalDiscountApplied,
                appliedCouponInstances);
    }
}