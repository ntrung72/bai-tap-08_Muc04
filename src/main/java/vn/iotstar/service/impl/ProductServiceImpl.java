package vn.iotstar.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.ProductRequest;
import vn.iotstar.model.Category;
import vn.iotstar.model.Product;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ProductService;
import vn.iotstar.util.TextEncodingUtils;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository repository, CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    @Override
    public List<Product> findAll(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        List<Product> products = value.isEmpty() ? repository.findAllByOrderByIdDesc()
                : repository.findByNameContainingIgnoreCaseOrderByIdDesc(value);
        products.forEach(this::normalizeText);
        return products;
    }

    @Override
    public Product findById(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có mã " + id + "."));
        normalizeText(product);
        return product;
    }

    @Override
    @Transactional
    public Product create(ProductRequest request) {
        Product product = new Product();
        copyRequest(request, product);
        return repository.save(product);
    }

    @Override
    @Transactional
    public Product update(Integer id, ProductRequest request) {
        Product product = findById(id);
        copyRequest(request, product);
        return repository.save(product);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        findById(id);
        repository.deleteById(id);
        repository.flush();
    }

    @Override
    public boolean nameExists(String name, Integer excludedId) {
        if (name == null || name.isBlank()) return false;
        String normalizedName = TextEncodingUtils.normalize(name).trim();
        return excludedId == null ? repository.existsByNameIgnoreCase(normalizedName)
                : repository.existsByNameIgnoreCaseAndIdNot(normalizedName, excludedId);
    }

    private void copyRequest(ProductRequest request, Product product) {
        Category category = categoryService.findById(request.getCategoryId());
        product.setName(TextEncodingUtils.normalize(request.getName()).trim());
        product.setQuantity(request.getQuantity());
        product.setPrice(request.getPrice());
        product.setImage(trimToNull(request.getImage()));
        product.setDescription(trimToNull(request.getDescription()));
        product.setCategory(category);
    }

    private void normalizeText(Product product) {
        product.setName(TextEncodingUtils.normalize(product.getName()));
        product.setDescription(TextEncodingUtils.normalize(product.getDescription()));
        if (product.getCategory() != null) {
            product.getCategory().setName(TextEncodingUtils.normalize(product.getCategory().getName()));
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return TextEncodingUtils.normalize(value).trim();
    }
}
