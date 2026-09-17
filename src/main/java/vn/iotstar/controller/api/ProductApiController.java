package vn.iotstar.controller.api;

import java.math.BigDecimal;
import java.util.UUID;

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
import vn.iotstar.model.Product;
import vn.iotstar.model.Response;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.IStorageService;
import vn.iotstar.service.ProductService;

@RestController
@RequestMapping(path = "/api/product")
public class ProductApiController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final IStorageService storageService;

    public ProductApiController(
            ProductService productService,
            CategoryService categoryService,
            IStorageService storageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<?> getAllProduct(
            @RequestParam(defaultValue = "") String keyword) {
        return new ResponseEntity<Response>(
                new Response(
                        true,
                        "Thành công",
                        productService.findAll(keyword)),
                HttpStatus.OK);
    }

    @PostMapping(path = "/getProduct")
    public ResponseEntity<?> getProduct(
            @Validated @RequestParam("id") Integer id) {
        try {
            Product product = productService.findById(id);
            return new ResponseEntity<Response>(
                    new Response(true, "Thành công", product),
                    HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, ex.getMessage(), null),
                    HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addProduct")
    public ResponseEntity<?> addProduct(
            @Validated @RequestParam("productName") String productName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @Validated @RequestParam("unitPrice") BigDecimal unitPrice,
            @RequestParam(value = "description", defaultValue = "") String description,
            @Validated @RequestParam("categoryId") Integer categoryId,
            @Validated @RequestParam("quantity") Integer quantity) {
        ResponseEntity<?> validationResponse = validateProduct(
                productName,
                unitPrice,
                quantity,
                categoryId,
                null);
        if (validationResponse != null) {
            return validationResponse;
        }

        Category category;
        try {
            category = categoryService.findById(categoryId);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, "Không tìm thấy Category", null),
                    HttpStatus.BAD_REQUEST);
        }

        Product product = new Product();
        String storedImage = storeImage(imageFile, "product");
        setProductData(
                product,
                productName,
                unitPrice,
                quantity,
                description,
                category,
                storedImage);

        try {
            productService.save(product);
            return new ResponseEntity<Response>(
                    new Response(true, "Thêm Thành công", product),
                    HttpStatus.OK);
        } catch (RuntimeException ex) {
            deleteImage(storedImage);
            throw ex;
        }
    }

    @PutMapping(path = "/updateProduct")
    public ResponseEntity<?> updateProduct(
            @Validated @RequestParam("productId") Integer productId,
            @Validated @RequestParam("productName") String productName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @Validated @RequestParam("unitPrice") BigDecimal unitPrice,
            @RequestParam(value = "description", defaultValue = "") String description,
            @Validated @RequestParam("categoryId") Integer categoryId,
            @Validated @RequestParam("quantity") Integer quantity) {
        Product product;

        try {
            product = productService.findById(productId);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, "Không tìm thấy Product", null),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> validationResponse = validateProduct(
                productName,
                unitPrice,
                quantity,
                categoryId,
                productId);
        if (validationResponse != null) {
            return validationResponse;
        }

        Category category;
        try {
            category = categoryService.findById(categoryId);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, "Không tìm thấy Category", null),
                    HttpStatus.BAD_REQUEST);
        }

        String oldImage = product.getImage();
        String storedImage = storeImage(imageFile, "product");
        String productImage = storedImage == null ? oldImage : storedImage;

        setProductData(
                product,
                productName,
                unitPrice,
                quantity,
                description,
                category,
                productImage);

        try {
            productService.save(product);
            if (storedImage != null) {
                deleteImage(oldImage);
            }
            return new ResponseEntity<Response>(
                    new Response(true, "Cập nhật Thành công", product),
                    HttpStatus.OK);
        } catch (RuntimeException ex) {
            deleteImage(storedImage);
            throw ex;
        }
    }

    @DeleteMapping(path = "/deleteProduct")
    public ResponseEntity<?> deleteProduct(
            @Validated @RequestParam("productId") Integer productId) {
        Product product;

        try {
            product = productService.findById(productId);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<Response>(
                    new Response(false, "Không tìm thấy Product", null),
                    HttpStatus.BAD_REQUEST);
        }

        productService.deleteById(productId);
        deleteImage(product.getImage());

        return new ResponseEntity<Response>(
                new Response(true, "Xóa Thành công", product),
                HttpStatus.OK);
    }

    private ResponseEntity<?> validateProduct(
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            Integer categoryId,
            Integer excludedId) {
        if (productName == null || productName.isBlank()) {
            return new ResponseEntity<Response>(
                    new Response(false, "Tên sản phẩm không được để trống.", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            return new ResponseEntity<Response>(
                    new Response(false, "Đơn giá phải lớn hơn hoặc bằng 0.", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (quantity == null || quantity < 0) {
            return new ResponseEntity<Response>(
                    new Response(false, "Số lượng phải lớn hơn hoặc bằng 0.", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (categoryId == null || categoryId <= 0) {
            return new ResponseEntity<Response>(
                    new Response(false, "Danh mục không hợp lệ.", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (productService.nameExists(productName, excludedId)) {
            return new ResponseEntity<Response>(
                    new Response(false, "Sản phẩm này đã tồn tại trong hệ thống", null),
                    HttpStatus.BAD_REQUEST);
        }

        return null;
    }

    private void setProductData(
            Product product,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            String description,
            Category category,
            String image) {
        product.setName(productName);
        product.setPrice(unitPrice);
        product.setQuantity(quantity);
        product.setDescription(description);
        product.setCategory(category);
        product.setImage(image);
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
