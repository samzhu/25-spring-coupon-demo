package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>
 * 此控制器負責：
 * <ul>
 *     <li>顯示購物車頁面 ({@code GET /cart})，包含初始商品項目與可選優惠券。</li>
 *     <li>接收購物車內容並計算最終價格 ({@code POST /cart/calculate})，應用選定之優惠券。</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    /**
     * 建構一個新的 {@code CartController}。
     *
     * @param cartService 購物車服務，用於處理購物車邏輯。
     * @param productRepository 商品儲存庫，用於獲取商品資訊。
     * @param couponRepository 優惠券儲存庫，用於獲取優惠券資訊。
     */
    public CartController(CartService cartService, ProductRepository productRepository, CouponRepository couponRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    /**
     * 處理 {@code GET /cart} 請求，顯示購物車頁面。
     * <p>
     * 此方法會將所有商品、所有優惠券以及一組預設的購物車項目加入模型中，
     * 並渲染 {@code checkout} 視圖。
     * </p>
     *
     * @param model Spring MVC 模型，用於將數據傳遞給視圖。
     * @return 視圖名稱 ({@code "checkout"})。
     */
    @GetMapping
    public String viewCartPage(Model model) {
        model.addAttribute("allProducts", productRepository.findAll());
        model.addAttribute("allCoupons", couponRepository.findAll());
        model.addAttribute("initialFixedCartItemsForDisplay", getInitialFixedCartItemsForDisplay());
        log.info("提供 checkout.mustache 頁面骨架及初始數據 (複選優惠券版)。");
        return "checkout";
    }
    
    /**
     * 準備一組固定的初始購物車項目用於前端顯示。
     * <p>
     * 這組項目是硬編碼的，主要用於演示或提供一個預設的購物車狀態。
     * </p>
     * @return 包含初始商品的 {@link CartItem} 列表。
     */
    private List<CartItem> getInitialFixedCartItemsForDisplay() {
        List<CartItem> displayItems = new ArrayList<>();
        // 定義一組預設的購物車項目 (商品ID與數量)
        List<CartItemInput> initialInputs = List.of(
            new CartItemInput("P001", 1), // 假設商品 P001 一件
            new CartItemInput("P002", 2)  // 假設商品 P002 兩件
        );
        for (CartItemInput input : initialInputs) {
            productRepository.findById(input.productId()).ifPresent(product -> {
                displayItems.add(new CartItem(product, input.quantity()));
            });
        }
        log.debug("準備了 {} 個初始購物車項目用於顯示。", displayItems.size());
        return displayItems;
    }

    /**
     * 處理 {@code POST /cart/calculate} AJAX 請求，計算購物車價格。
     * <p>
     * 此方法接收包含商品項目和選定優惠券的 {@link ShoppingCartInput}，
     * 然後調用 {@link CartService} 計算折扣後的總價、折扣金額等。
     * </p>
     *
     * @param shoppingCartInput 包含購物車商品和選定優惠券的輸入數據。
     * @return 如果輸入無效，返回 HTTP 400 (Bad Request)；否則，返回包含計算結果的 {@link CalculationResultDto} 及 HTTP 200 (OK)。
     */
    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<CalculationResultDto> calculateCart(@RequestBody ShoppingCartInput shoppingCartInput) {
        if (shoppingCartInput == null || shoppingCartInput.items() == null) {
            log.error("錯誤: /cart/calculate 收到的 shoppingCartInput 或其 items 為 null。輸入數據: {}", shoppingCartInput);
            return ResponseEntity.badRequest().build(); 
        }
        log.info("收到 /cart/calculate 請求: {}", shoppingCartInput);
        CalculationResultDto result = cartService.calculateCartPrice(shoppingCartInput);
        log.info("計算結果: {}", result);
        return ResponseEntity.ok(result);
    }
}