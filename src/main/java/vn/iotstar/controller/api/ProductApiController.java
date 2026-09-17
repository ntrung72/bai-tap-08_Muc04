package vn.iotstar.controller.api;

import java.util.List;

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
import vn.iotstar.dto.ProductRequest;
import vn.iotstar.dto.ProductResponse;
import vn.iotstar.model.Product;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.ImageStorageService;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {
    private final ProductService service;
    private final ImageStorageService imageStorageService;

    public ProductApiController(ProductService service, ImageStorageService imageStorageService) {
        this.service = service;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll(
            @RequestParam(defaultValue = "") String keyword) {
        List<ProductResponse> products = service.findAll(keyword).stream()
                .map(ProductResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công.", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin sản phẩm thành công.",
                new ProductResponse(service.findById(id))));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request) {
        validateDuplicateName(request.getName(), null);
        Product product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm sản phẩm thành công.", new ProductResponse(product)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createWithImage(
            @Valid @RequestPart("data") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        String storedImage = imageStorageService.store(image, "product");
        request.setImage(storedImage);
        try {
            return create(request);
        } catch (RuntimeException ex) {
            imageStorageService.delete(storedImage, "product");
            throw ex;
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody ProductRequest request) {
        service.findById(id);
        validateDuplicateName(request.getName(), id);
        Product product = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công.",
                new ProductResponse(product)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateWithImage(@PathVariable Integer id,
            @Valid @RequestPart("data") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        Product current = service.findById(id);
        String oldImage = current.getImage();
        String storedImage = imageStorageService.store(image, "product");
        request.setImage(storedImage == null ? oldImage : storedImage);

        try {
            ResponseEntity<ApiResponse<ProductResponse>> response = update(id, request);
            if (storedImage != null) imageStorageService.delete(oldImage, "product");
            return response;
        } catch (RuntimeException ex) {
            imageStorageService.delete(storedImage, "product");
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> delete(@PathVariable Integer id) {
        Product entity = service.findById(id);
        ProductResponse product = new ProductResponse(entity);
        service.deleteById(id);
        imageStorageService.delete(entity.getImage(), "product");
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công.", product));
    }

    private void validateDuplicateName(String name, Integer excludedId) {
        if (service.nameExists(name, excludedId)) {
            throw new IllegalStateException("Tên sản phẩm đã tồn tại.");
        }
    }
}
