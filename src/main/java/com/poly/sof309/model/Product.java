package com.poly.sof309.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(name = "uk_product_name", columnNames = "name"))
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, length = 60)
    private String category;
    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal price;
    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal oldPrice;
    @Column(length = 1000)
    private String image;
    @Column(nullable = false)
    private Integer quantity;

    protected Product() { }
    public Product(String name, String category, BigDecimal price, BigDecimal oldPrice, String image, Integer quantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.oldPrice = oldPrice;
        this.image = image;
        this.quantity = quantity;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public String getImage() { return image; }
    public Integer getQuantity() { return quantity; }
    public void update(String name, String category, BigDecimal price, BigDecimal oldPrice, String image, Integer quantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.oldPrice = oldPrice;
        this.image = image;
        this.quantity = quantity;
    }
}
