package vn.iotstar.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.model.Category;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.CategoryService;
import vn.iotstar.util.TextEncodingUtils;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> findAll(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        List<Category> categories = value.isEmpty() ? repository.findAllByOrderByIdDesc()
                : repository.findByNameContainingIgnoreCaseOrderByIdDesc(value);
        categories.forEach(category -> category.setName(TextEncodingUtils.normalize(category.getName())));
        return categories;
    }

    @Override
    public Category findById(Integer id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục có mã " + id + "."));
        category.setName(TextEncodingUtils.normalize(category.getName()));
        return category;
    }

    @Override
    @Transactional
    public Category save(Category category) {
        category.setName(TextEncodingUtils.normalize(category.getName()).trim());
        if (category.getIcon() != null) category.setIcon(category.getIcon().trim());
        return repository.save(category);
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
        return excludedId == null ? repository.existsByNameIgnoreCase(name.trim())
                : repository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludedId);
    }
}
