package com.poly.sof309.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.sof309.controller.ProductController;
import com.poly.sof309.dto.ProductRequest;
import com.poly.sof309.model.Product;
import com.poly.sof309.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductService productService;

    @ParameterizedTest(name = "Từ chối giá bán {0}")
    @CsvSource({"0", "-1"})
    @DisplayName("TC_PRODUCT_PRICE_BOUNDARY: Giá bán phải lớn hơn 0")
    void createRejectsNonPositivePrice(String price) throws Exception {
        ProductRequest request = productRequest(new BigDecimal(price), BigDecimal.ZERO, 0);
        mockMvc.perform(post("/api/products").sessionAttr("accountId", 17L)
                        .contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @ParameterizedTest(name = "Từ chối số lượng {0}")
    @CsvSource({"-1", "-10"})
    @DisplayName("TC_PRODUCT_QUANTITY_BOUNDARY: Số lượng không được âm")
    void createRejectsNegativeQuantity(int quantity) throws Exception {
        ProductRequest request = productRequest(BigDecimal.ONE, BigDecimal.ZERO, quantity);
        mockMvc.perform(post("/api/products").sessionAttr("accountId", 17L)
                        .contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("TC_PRODUCT_CREATE_UNAUTH: Không đăng nhập thì không được tạo sản phẩm")
    void createRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/products").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest(BigDecimal.TEN, BigDecimal.ZERO, 1))))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("TC_PRODUCT_CREATE_SUCCESS: Tạo sản phẩm hợp lệ trả HTTP 201")
    void createSuccess() throws Exception {
        ProductRequest request = productRequest(new BigDecimal("100"), new BigDecimal("120"), 4);
        when(productService.create(any(ProductRequest.class))).thenReturn(
                new Product("QA Phone", "Công nghệ", request.price(), request.oldPrice(), "", request.quantity()));

        mockMvc.perform(post("/api/products").sessionAttr("accountId", 17L)
                        .contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("QA Phone"))
                .andExpect(jsonPath("$.quantity").value(4));
    }

    private ProductRequest productRequest(BigDecimal price, BigDecimal oldPrice, Integer quantity) {
        return new ProductRequest("QA Phone", "Công nghệ", price, oldPrice, "", quantity);
    }
}
