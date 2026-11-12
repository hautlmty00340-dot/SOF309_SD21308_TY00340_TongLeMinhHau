package com.poly.sof309.service;

import com.poly.sof309.dto.ProductRequest;
import com.poly.sof309.model.Product;
import com.poly.sof309.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;
    public ProductService(ProductRepository products) { this.products = products; }
    public List<Product> findAll() { return products.findAll(); }
    public Product create(ProductRequest request) {
        if (products.existsByNameIgnoreCase(request.name().trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên sản phẩm đã tồn tại");
        return products.save(new Product(request.name().trim(), request.category().trim(), request.price(), request.oldPrice(), request.image(), request.quantity()));
    }
    public Product update(Long id, ProductRequest request) {
        Product product = getRequired(id);
        products.findByNameIgnoreCase(request.name().trim()).filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên sản phẩm đã tồn tại"); });
        product.update(request.name().trim(), request.category().trim(), request.price(), request.oldPrice(), request.image(), request.quantity());
        return products.save(product);
    }
    public void delete(Long id) { products.delete(getRequired(id)); }
    private Product getRequired(Long id) {
        return products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }
}
