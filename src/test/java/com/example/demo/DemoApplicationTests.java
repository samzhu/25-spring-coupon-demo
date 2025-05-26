package com.example.demo;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.CalculationResultDto;
import com.example.demo.dto.CartItemInput;
import com.example.demo.dto.ShoppingCartInput;
import com.example.demo.model.Coupon;
import com.example.demo.model.Product;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CartService cartService;

	@MockitoBean
	private ProductRepository productRepository;

	@MockitoBean
	private CouponRepository couponRepository;

	private Product product1;
	private Product product2;
	private Coupon coupon1;

	@BeforeEach
	void setUp() {
		// 設置測試數據
		product1 = new Product("P001", "測試商品1", 100);
		product2 = new Product("P002", "測試商品2", 200);
		coupon1 = new Coupon("C001", "測試優惠券", 50);

		// 模擬 Repository 行為
		when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
		when(couponRepository.findAll()).thenReturn(Arrays.asList(coupon1));
		when(productRepository.findById("P001")).thenReturn(java.util.Optional.of(product1));
		when(productRepository.findById("P002")).thenReturn(java.util.Optional.of(product2));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void viewCartPage_ShouldReturnCheckoutView() throws Exception {
		mockMvc.perform(get("/cart"))
			.andExpect(status().isOk())
			.andExpect(view().name("checkout"))
			.andExpect(model().attributeExists("allProducts"))
			.andExpect(model().attributeExists("allCoupons"))
			.andExpect(model().attributeExists("initialFixedCartItemsForDisplay"));

		verify(productRepository).findAll();
		verify(couponRepository).findAll();
	}

	@Test
	void calculateCart_WithValidInput_ShouldReturnCalculationResult() throws Exception {
		// 準備測試數據
		List<CartItemInput> items = Arrays.asList(
			new CartItemInput("P001", 2),
			new CartItemInput("P002", 1)
		);
		ShoppingCartInput input = new ShoppingCartInput(items, Arrays.asList("C001"));
		
		CalculationResultDto expectedResult = new CalculationResultDto(
			400,  // 原始總額 (BigDecimal 改為 Integer)
			350,  // 折扣後總額 (BigDecimal 改為 Integer)
			50,   // 總折扣金額 (BigDecimal 改為 Integer)
			Arrays.asList(coupon1)     // 使用的優惠券
		);

		when(cartService.calculateTotalsFromDto(input)).thenReturn(expectedResult);

		// 執行測試
		mockMvc.perform(post("/cart/calculate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(input)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.originalTotal").value(400))
			.andExpect(jsonPath("$.discountedTotal").value(350))
			.andExpect(jsonPath("$.totalDiscountAmount").value(50))
			.andExpect(jsonPath("$.appliedCoupons[0].code").value("C001"));

		verify(cartService).calculateTotalsFromDto(input);
	}

	@Test
	void calculateCart_WithNullInput_ShouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/cart/calculate")
				.contentType(MediaType.APPLICATION_JSON)
				.content("null"))
			.andExpect(status().isBadRequest());
	}
}
