package com.poly.sof309.config;

import com.poly.sof309.model.Product;
import com.poly.sof309.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() > 0) return;
            products.saveAll(List.of(
                    new Product("Tai nghe Bluetooth PulseTune Pro", "Công nghệ", new BigDecimal("780000"), new BigDecimal("1500000"), "", 20),
                    new Product("Đồng hồ thông minh AMOLED", "Công nghệ", new BigDecimal("970000"), new BigDecimal("1290000"), "", 15),
                    new Product("Giày thể thao Urban", "Thời trang", new BigDecimal("590000"), new BigDecimal("890000"), "", 30)
            ));
        };
    }
}
