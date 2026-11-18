package com.poly.sof309.services;

import com.poly.sof309.dto.ProductRequest;
import com.poly.sof309.model.Product;
import com.poly.sof309.repository.ProductRepository;
import com.poly.sof309.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductService productService = new ProductService(products);

    @Test
    @DisplayName("TC_PRODUCT_DUPLICATE: Ném 409 khi tạo sản phẩm trùng tên")
    void createDuplicateNameThrowsConflict() {
        when(products.existsByNameIgnoreCase("qa phone")).thenReturn(true);
        ProductRequest request = request("QA Phone");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> productService.create(request));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(products, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("TC_PRODUCT_NOT_FOUND: Ném 404 khi cập nhật ID không tồn tại")
    void updateMissingProductThrowsNotFound() {
        when(products.findById(9876L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> productService.update(9876L, request("QA Phone")));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(products, never()).save(any(Product.class));
    }

    private ProductRequest request(String name) {
        return new ProductRequest(name, "Công nghệ", BigDecimal.TEN, BigDecimal.ZERO, "", 1);
    }
}
