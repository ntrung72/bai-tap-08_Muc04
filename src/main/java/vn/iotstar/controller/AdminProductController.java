package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "product");
        model.addAttribute("pageTitle", "Quản lý sản phẩm bằng AJAX");
        model.addAttribute("contentPage", "/WEB-INF/views/admin/product/ajax.jsp");
        model.addAttribute("pageScript", "/js/product-ajax.js");
        return "admin";
    }
}
