package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.CalculationResultDto;
import com.example.demo.dto.CartItemInput;
import com.example.demo.dto.ShoppingCartInput;
import com.example.demo.model.CartItem;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.CartService;

/**
 * 處理購物車相關請求的控制器。
 * GET /cart 用於提供 HTML 頁面骨架及靜態數據。
 * POST /cart/calculate 作為 AJAX 端點，接收購物車數據並返回計算結果。
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public CartController(CartService cartService, ProductRepository productRepository, CouponRepository couponRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        System.out.println("CartController (AJAX版, 複選優惠券) 已初始化。");
    }

    @GetMapping
    public String viewCartPage(Model model) {
        model.addAttribute("allProducts", productRepository.findAll());
        model.addAttribute("allCoupons", couponRepository.findAll());
        model.addAttribute("initialFixedCartItemsForDisplay", getInitialFixedCartItemsForDisplay());
        System.out.println("提供 checkout.mustache 頁面骨架及初始數據 (複選優惠券版)。");
        return "checkout";
    }
    
    private List<CartItem> getInitialFixedCartItemsForDisplay() {
        List<CartItem> displayItems = new ArrayList<>();
        List<CartItemInput> initialInputs = List.of(
            new CartItemInput("P001", 1),
            new CartItemInput("P002", 2)
        );
        for (CartItemInput input : initialInputs) {
            productRepository.findById(input.productId()).ifPresent(product -> {
                displayItems.add(new CartItem(product, input.quantity()));
            });
        }
        return displayItems;
    }

    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<CalculationResultDto> calculateCart(@RequestBody ShoppingCartInput shoppingCartInput) {
        if (shoppingCartInput == null || shoppingCartInput.items() == null) {
            System.err.println("錯誤: /cart/calculate 收到的 shoppingCartInput 為 null 或 items 為 null");
            return ResponseEntity.badRequest().build(); 
        }
        System.out.println("收到 /cart/calculate 請求 (複選): " + shoppingCartInput);
        CalculationResultDto result = cartService.calculateTotalsFromDto(shoppingCartInput);
        System.out.println("計算結果 (複選): " + result);
        return ResponseEntity.ok(result);
    }
}