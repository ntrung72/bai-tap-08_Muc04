package vn.iotstar.dto;

import java.math.BigDecimal;

import vn.iotstar.model.Product;

public class ProductResponse {
    private final Integer id;
    private final String name;
    private final Integer quantity;
    private final BigDecimal price;
    private final String image;
    private final String description;
    private final Integer categoryId;
    private final String categoryName;

    public ProductResponse(Product product) {
        id = product.getId();
        name = product.getName();
        quantity = product.getQuantity();
        price = product.getPrice();
        image = product.getImage();
        description = product.getDescription();
        categoryId = product.getCategory().getId();
        categoryName = product.getCategory().getName();
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public String getImage() { return image; }
    public String getDescription() { return description; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
}
