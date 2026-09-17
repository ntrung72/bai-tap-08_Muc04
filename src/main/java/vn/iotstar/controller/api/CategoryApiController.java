package vn.iotstar.controller.api;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.dto.CategoryRequest;
import vn.iotstar.model.Category;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ImageStorageService;

@RestController
@RequestMapping("/api/category")
public class CategoryApiController {
    private final CategoryService service;
    private final ImageStorageService imageStorageService;

    public CategoryApiController(CategoryService service, ImageStorageService imageStorageService) {
        this.service = service;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll(
            @RequestParam(defaultValue = "") String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công.",
                service.findAll(keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin danh mục thành công.",
                service.findById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Category>> create(@Valid @RequestBody CategoryRequest request) {
        validateDuplicateName(request.getName(), null);
        Category category = new Category();
        copyRequest(request, category);
        Category saved = service.save(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm danh mục thành công.", saved));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Category>> createWithImage(
            @Valid @RequestPart("data") CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        String storedImage = imageStorageService.store(image, "category");
        request.setIcon(storedImage);
        try {
            return create(request);
        } catch (RuntimeException ex) {
            imageStorageService.delete(storedImage, "category");
            throw ex;
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Category>> update(@PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = service.findById(id);
        validateDuplicateName(request.getName(), id);
        copyRequest(request, category);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công.",
                service.save(category)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Category>> updateWithImage(@PathVariable Integer id,
            @Valid @RequestPart("data") CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        Category current = service.findById(id);
        String oldImage = current.getIcon();
        String storedImage = imageStorageService.store(image, "category");
        request.setIcon(storedImage == null ? oldImage : storedImage);

        try {
            ResponseEntity<ApiResponse<Category>> response = update(id, request);
            if (storedImage != null) imageStorageService.delete(oldImage, "category");
            return response;
        } catch (RuntimeException ex) {
            imageStorageService.delete(storedImage, "category");
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> delete(@PathVariable Integer id) {
        Category category = service.findById(id);
        try {
            service.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Không thể xóa vì danh mục đang được sản phẩm sử dụng.");
        }
        imageStorageService.delete(category.getIcon(), "category");
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công.", category));
    }

    private void validateDuplicateName(String name, Integer excludedId) {
        if (service.nameExists(name, excludedId)) {
            throw new IllegalStateException("Tên danh mục đã tồn tại.");
        }
    }

    private void copyRequest(CategoryRequest request, Category category) {
        category.setName(request.getName());
        category.setIcon(request.getIcon());
    }
}
