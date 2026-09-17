package vn.iotstar.service;

import java.util.List;
import vn.iotstar.model.Category;

public interface CategoryService {
    List<Category> findAll(String keyword);
    Category findById(Integer id);
    Category save(Category category);
    void deleteById(Integer id);
    boolean nameExists(String name, Integer excludedId);
}
