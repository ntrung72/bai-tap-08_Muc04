package vn.iotstar.service;

import java.util.List;

import vn.iotstar.model.Product;

public interface ProductService {
    List<Product> findAll(String keyword);

    Product findById(Integer id);

    Product save(Product product);

    void deleteById(Integer id);

    boolean nameExists(String name, Integer excludedId);
}
