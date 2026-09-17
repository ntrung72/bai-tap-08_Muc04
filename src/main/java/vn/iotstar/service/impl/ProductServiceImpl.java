package vn.iotstar.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.model.Product;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.ProductService;
import vn.iotstar.util.TextEncodingUtils;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Product> findAll(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        List<Product> products;

        if (value.isEmpty()) {
            products = repository.findAllByOrderByIdDesc();
        } else {
            products = repository.findByNameContainingIgnoreCaseOrderByIdDesc(value);
        }

        products.forEach(this::normalizeText);
        return products;
    }

    @Override
    public Product findById(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sản phẩm có mã " + id + "."));
        normalizeText(product);
        return product;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        product.setName(TextEncodingUtils.normalize(product.getName()).trim());
        product.setImage(trimToNull(product.getImage()));
        product.setDescription(trimToNull(product.getDescription()));
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
        if (name == null || name.isBlank()) {
            return false;
        }

        String normalizedName = TextEncodingUtils.normalize(name).trim();
        if (excludedId == null) {
            return repository.existsByNameIgnoreCase(normalizedName);
        }

        return repository.existsByNameIgnoreCaseAndIdNot(normalizedName, excludedId);
    }

    private void normalizeText(Product product) {
        product.setName(TextEncodingUtils.normalize(product.getName()));
        product.setDescription(TextEncodingUtils.normalize(product.getDescription()));

        if (product.getCategory() != null) {
            product.getCategory().setName(
                    TextEncodingUtils.normalize(product.getCategory().getName()));
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TextEncodingUtils.normalize(value).trim();
    }
}
