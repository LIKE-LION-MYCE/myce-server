package com.myce.expo.service.impl;

import com.myce.expo.dto.CategoryResponse;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.service.CategoryService;
import com.myce.expo.service.mapper.CategoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;

  @Override
  public List<CategoryResponse> getCategories() {
    return categoryRepository.findAll()
        .stream()
        .map(CategoryMapper::toDto)
        .toList();
  }
}
