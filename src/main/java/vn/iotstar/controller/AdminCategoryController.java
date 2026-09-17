package vn.iotstar.controller;

import java.util.UUID;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.iotstar.model.Category;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.IStorageService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService service;
    private final IStorageService storageService;

    public AdminCategoryController(
            CategoryService service,
            IStorageService storageService) {
        this.service = service;
        this.storageService = storageService;
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
    public String editForm(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirect) {
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
    public String save(
            @Valid @ModelAttribute("category") Category category,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirect) {
        if (service.nameExists(category.getName(), category.getId())) {
            result.rejectValue("name", "duplicate", "Tên danh mục đã tồn tại.");
        }

        if (result.hasErrors()) {
            String formTitle;
            if (category.getId() == null) {
                formTitle = "Thêm danh mục";
            } else {
                formTitle = "Cập nhật danh mục";
            }
            model.addAttribute("formTitle", formTitle);
            model.addAttribute("activeMenu", "category");
            return "admin/category/form";
        }

        boolean creating = category.getId() == null;
        String oldImage = null;
        if (!creating) {
            oldImage = service.findById(category.getId()).getIcon();
        }

        String storedImage = storeImage(imageFile, "category");
        if (storedImage == null) {
            category.setIcon(oldImage);
        } else {
            category.setIcon(storedImage);
        }

        try {
            service.save(category);
            if (storedImage != null) {
                deleteImage(oldImage);
            }
        } catch (RuntimeException ex) {
            deleteImage(storedImage);
            throw ex;
        }

        if (creating) {
            redirect.addFlashAttribute("success", "Thêm danh mục thành công.");
        } else {
            redirect.addFlashAttribute("success", "Cập nhật danh mục thành công.");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Integer id,
            RedirectAttributes redirect) {
        try {
            String image = service.findById(id).getIcon();
            service.deleteById(id);
            deleteImage(image);
            redirect.addFlashAttribute("success", "Xóa danh mục thành công.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute(
                    "error",
                    "Không thể xóa vì danh mục đang được sản phẩm sử dụng.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        UUID uuid = UUID.randomUUID();
        String filename = storageService.getSorageFilename(
                file,
                uuid.toString());
        String storeFilename = folder + "/" + filename;
        storageService.store(file, storeFilename);
        return storeFilename;
    }

    private void deleteImage(String storeFilename) {
        if (storeFilename == null || storeFilename.isBlank()) {
            return;
        }

        try {
            storageService.delete(storeFilename);
        } catch (Exception ex) {
        }
    }
}
