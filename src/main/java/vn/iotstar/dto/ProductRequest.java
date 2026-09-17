package vn.iotstar.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống.")
    @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2 đến 255 ký tự.")
    private String name;

    @NotNull(message = "Số lượng không được để trống.")
    @PositiveOrZero(message = "Số lượng phải lớn hơn hoặc bằng 0.")
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải lớn hơn hoặc bằng 0.")
    private BigDecimal price;

    @Size(max = 255, message = "Đường dẫn ảnh không được vượt quá 255 ký tự.")
    private String image;

    @Size(max = 4000, message = "Mô tả không được vượt quá 4000 ký tự.")
    private String description;

    @NotNull(message = "Danh mục không được để trống.")
    @Positive(message = "Danh mục không hợp lệ.")
    private Integer categoryId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}
