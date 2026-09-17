package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống.")
    @Size(min = 2, max = 255, message = "Tên danh mục phải từ 2 đến 255 ký tự.")
    private String name;

    @Size(max = 255, message = "Đường dẫn biểu tượng không được vượt quá 255 ký tự.")
    private String icon;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
