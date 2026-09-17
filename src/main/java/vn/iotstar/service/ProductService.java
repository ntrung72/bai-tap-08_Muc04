package vn.iotstar.service;

import java.util.List;

import vn.iotstar.dto.ProductRequest;
import vn.iotstar.model.Product;

public interface ProductService {
    List<Product> findAll(String keyword);
    Product findById(Integer id);
    Product create(ProductRequest request);
    Product update(Integer id, ProductRequest request);
    void deleteById(Integer id);
    boolean nameExists(String name, Integer excludedId);
}
