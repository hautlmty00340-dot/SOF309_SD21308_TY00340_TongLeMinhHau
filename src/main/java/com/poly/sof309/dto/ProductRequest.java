package com.poly.sof309.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Tên sản phẩm không được để trống") @Size(max = 160, message = "Tên sản phẩm tối đa 160 ký tự") String name,
        @NotBlank(message = "Danh mục không được để trống") @Size(max = 60, message = "Danh mục tối đa 60 ký tự") String category,
        @NotNull(message = "Giá bán không được để trống") @DecimalMin(value = "1", message = "Giá bán phải lớn hơn 0") BigDecimal price,
        @NotNull(message = "Giá gốc không được để trống") @DecimalMin(value = "0", message = "Giá gốc không được âm") BigDecimal oldPrice,
        @Size(max = 1000, message = "Đường dẫn ảnh tối đa 1000 ký tự") String image,
        @NotNull(message = "Số lượng không được để trống") @Min(value = 0, message = "Số lượng không được âm") Integer quantity) { }
