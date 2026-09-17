package vn.iotstar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByNameContainingIgnoreCaseOrderByIdDesc(String keyword);
    List<Category> findAllByOrderByIdDesc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
