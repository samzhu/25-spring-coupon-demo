package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.demo.model.Coupon;
import com.example.demo.model.Product;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.ProductRepository;

/**
 * 應用程式啟動時初始化範例數據。
 * 實現 CommandLineRunner 接口，使其在 Spring Boot 應用程式上下文載入完成後執行 run 方法。
 */
@Component
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    /**
     * 建構 DataInitializer。
     * 
     * @param productRepository 產品倉庫的依賴注入
     * @param couponRepository  優惠券倉庫的依賴注入
     */
    public DataInitializer(ProductRepository productRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() throws Exception {
        productRepository.save(new Product("P001", "高效能筆記型電腦", 700));
        productRepository.save(new Product("P002", "無線降噪耳機", 300));
        productRepository.save(new Product("P003", "智能手錶", 8000));
        productRepository.save(new Product("P004", "4K顯示器", 12000));
        log.debug("範例產品已初始化");

        couponRepository.save(new Coupon("SAVE100", "折抵券：現折100元", 100));
        couponRepository.save(new Coupon("SAVE500", "折抵券：現折500元", 500));
        couponRepository.save(new Coupon("BIGSAVE", "超級折抵券：現折1000元", 1000));
        // 新增一張優惠券以測試多選
        couponRepository.save(new Coupon("BONUS200", "額外獎勵：再折200元", 200));
        log.debug("範例優惠券已初始化");
    }

}
