package com.example.demo.service;

import com.example.demo.dto.CartItemInput;
import com.example.demo.dto.ShoppingCartInput;
import com.example.demo.exception.ExcessiveDiscountException;
import com.example.demo.model.Coupon;
import com.example.demo.model.Product;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void testCalculateCartPrice_shouldThrowExcessiveDiscountException_whenDiscountExceedsTotal() {
        // Arrange
        // Product price is 100
        Product product1 = new Product("P001", "Product 1", 100);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product1));

        // Coupon discount is 150, which is > 100
        Coupon excessiveCoupon = new Coupon("COUPON_EXCESSIVE", "Excessive Discount Coupon", 150);
        excessiveCoupon.setId(1L); // Assuming Coupon has an ID field that might be accessed
        when(couponRepository.findByCode("COUPON_EXCESSIVE")).thenReturn(Optional.of(excessiveCoupon));

        // ShoppingCartInput uses List<CartItemInput> and List<String> for coupon codes
        CartItemInput itemInput = new CartItemInput("P001", 1);
        ShoppingCartInput cartInput = new ShoppingCartInput(Collections.singletonList(itemInput), Collections.singletonList("COUPON_EXCESSIVE"));

        // Act & Assert
        ExcessiveDiscountException exception = assertThrows(ExcessiveDiscountException.class, () -> {
            cartService.calculateCartPrice(cartInput);
        });
        assertEquals("所選優惠券總折價已達上限，無法套用更多優惠券", exception.getMessage());
    }

    // Minimal test for a valid coupon to ensure the main path works
    @Test
    void testCalculateCartPrice_shouldApplyValidCoupon() {
        // Arrange
        Product product1 = new Product("P001", "Product 1", 100);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product1));

        Coupon validCoupon = new Coupon("COUPON_VALID", "Valid Discount Coupon", 50);
        validCoupon.setId(2L);
        when(couponRepository.findByCode("COUPON_VALID")).thenReturn(Optional.of(validCoupon));

        CartItemInput itemInput = new CartItemInput("P001", 1);
        ShoppingCartInput cartInput = new ShoppingCartInput(Collections.singletonList(itemInput), Collections.singletonList("COUPON_VALID"));

        // Act
        var result = cartService.calculateCartPrice(cartInput);

        // Assert
        assertEquals(100, result.rawTotalPrice()); // Raw total
        assertEquals(50, result.finalPrice());     // Final price after 50 discount
        assertEquals(50, result.totalDiscountAmount()); // Discount amount
        assertEquals(1, result.appliedCoupons().size());
        assertEquals("COUPON_VALID", result.appliedCoupons().get(0).getCode());
    }

    // Test for no coupon applied
    @Test
    void testCalculateCartPrice_noCouponApplied() {
        // Arrange
        Product product1 = new Product("P001", "Product 1", 100);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product1));

        CartItemInput itemInput = new CartItemInput("P001", 1);
        // No coupon codes provided
        ShoppingCartInput cartInput = new ShoppingCartInput(Collections.singletonList(itemInput), Collections.emptyList());

        // Act
        var result = cartService.calculateCartPrice(cartInput);

        // Assert
        assertEquals(100, result.rawTotalPrice());
        assertEquals(100, result.finalPrice());
        assertEquals(0, result.totalDiscountAmount());
        assertEquals(0, result.appliedCoupons().size());
    }
}
