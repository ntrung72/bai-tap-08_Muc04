package vn.iotstar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByOrderByIdDesc();
    List<Product> findByNameContainingIgnoreCaseOrderByIdDesc(String keyword);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
