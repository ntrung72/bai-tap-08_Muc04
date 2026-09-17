package vn.iotstar.controller.api;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.model.Category;
import vn.iotstar.model.Response;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping(path = "/api/category")
public class CategoryApiController {
    private final CategoryService categoryService;
    private final IStorageService storageService;

    public CategoryApiController(
            CategoryService categoryService,
            IStorageService storageService) {
        this.categoryService = categoryService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCategory(
            @RequestParam(defaultValue = "") String keyword) {
        return new ResponseEntity<Response>(
                new Response(
                        true,
                        "Thành công",
                        categoryService.findAll(keyword)),
                HttpStatus.OK);
    }

    @PostMapping(path = "/getCategory")
    public ResponseEntity<?> getCategory(
            @Validated @RequestParam("id") Integer id) {
        try {
            Category category = categoryService.findById(id);
            return new ResponseEntity<Response>(
                    new Response(true, "Thành công", category),
                    HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, ex.getMessage(), null),
                    HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addCategory")
    public ResponseEntity<?> addCategory(
            @Validated @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        if (categoryName == null || categoryName.isBlank()) {
            return new ResponseEntity<Response>(
                    new Response(false, "Tên danh mục không được để trống.", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (categoryService.nameExists(categoryName, null)) {
            return new ResponseEntity<Response>(
                    new Response(false, "Category đã tồn tại trong hệ thống", null),
                    HttpStatus.BAD_REQUEST);
        }

        Category category = new Category();
        String storedImage = storeImage(icon, "category");
        category.setName(categoryName);
        category.setIcon(storedImage);

        try {
            categoryService.save(category);
            return new ResponseEntity<Response>(
                    new Response(true, "Thêm Thành công", category),
                    HttpStatus.OK);
        } catch (RuntimeException ex) {
            deleteImage(storedImage);
            throw ex;
        }
    }

    @PutMapping(path = "/updateCategory")
    public ResponseEntity<?> updateCategory(
            @Validated @RequestParam("categoryId") Integer categoryId,
            @Validated @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        Category category;

        try {
            category = categoryService.findById(categoryId);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, "Không tìm thấy Category", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (categoryName == null || categoryName.isBlank()) {
            return new ResponseEntity<Response>(
                    new Response(false, "Tên danh mục không được để trống.", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (categoryService.nameExists(categoryName, categoryId)) {
            return new ResponseEntity<Response>(
                    new Response(false, "Category đã tồn tại trong hệ thống", null),
                    HttpStatus.BAD_REQUEST);
        }

        String oldImage = category.getIcon();
        String storedImage = storeImage(icon, "category");
        category.setName(categoryName);

        if (storedImage != null) {
            category.setIcon(storedImage);
        }

        try {
            categoryService.save(category);
            if (storedImage != null) {
                deleteImage(oldImage);
            }
            return new ResponseEntity<Response>(
                    new Response(true, "Cập nhật Thành công", category),
                    HttpStatus.OK);
        } catch (RuntimeException ex) {
            deleteImage(storedImage);
            throw ex;
        }
    }

    @DeleteMapping(path = "/deleteCategory")
    public ResponseEntity<?> deleteCategory(
            @Validated @RequestParam("categoryId") Integer categoryId) {
        Category category;

        try {
            category = categoryService.findById(categoryId);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, "Không tìm thấy Category", null),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            categoryService.deleteById(categoryId);
            deleteImage(category.getIcon());
            return new ResponseEntity<Response>(
                    new Response(true, "Xóa Thành công", category),
                    HttpStatus.OK);
        } catch (DataIntegrityViolationException ex) {
            return new ResponseEntity<Response>(
                    new Response(
                            false,
                            "Không thể xóa vì danh mục đang được sản phẩm sử dụng.",
                            null),
                    HttpStatus.BAD_REQUEST);
        }
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
