package vn.iotstar.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.iotstar.model.Category;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ImageStorageService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService service;
    private final ImageStorageService imageStorageService;

    public AdminCategoryController(CategoryService service, ImageStorageService imageStorageService) {
        this.service = service;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "category");
        model.addAttribute("pageTitle", "Quản lý danh mục bằng AJAX");
        model.addAttribute("contentPage", "/WEB-INF/views/admin/category/ajax.jsp");
        model.addAttribute("pageScript", "/js/category-ajax.js");
        return "admin";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("formTitle", "Thêm danh mục");
        model.addAttribute("activeMenu", "category");
        return "admin/category/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("category", service.findById(id));
            model.addAttribute("formTitle", "Cập nhật danh mục");
            model.addAttribute("activeMenu", "category");
            return "admin/category/form";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") Category category, BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model, RedirectAttributes redirect) {
        if (service.nameExists(category.getName(), category.getId())) {
            result.rejectValue("name", "duplicate", "Tên danh mục đã tồn tại.");
        }
        if (result.hasErrors()) {
            model.addAttribute("formTitle", category.getId() == null ? "Thêm danh mục" : "Cập nhật danh mục");
            model.addAttribute("activeMenu", "category");
            return "admin/category/form";
        }
        boolean creating = category.getId() == null;
        String oldImage = creating ? null : service.findById(category.getId()).getIcon();
        String storedImage = imageStorageService.store(imageFile, "category");
        category.setIcon(storedImage == null ? oldImage : storedImage);
        try {
            service.save(category);
            if (storedImage != null) imageStorageService.delete(oldImage, "category");
        } catch (RuntimeException ex) {
            imageStorageService.delete(storedImage, "category");
            throw ex;
        }
        redirect.addFlashAttribute("success", creating
                ? "Thêm danh mục thành công." : "Cập nhật danh mục thành công.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            String image = service.findById(id).getIcon();
            service.deleteById(id);
            imageStorageService.delete(image, "category");
            redirect.addFlashAttribute("success", "Xóa danh mục thành công.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "Không thể xóa vì danh mục đang được sản phẩm sử dụng.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
