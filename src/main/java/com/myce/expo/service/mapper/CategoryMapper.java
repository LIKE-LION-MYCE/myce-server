package com.myce.expo.service.mapper;

import com.myce.expo.dto.CategoryResponse;
import com.myce.expo.entity.Category;

public class CategoryMapper {
  public static CategoryResponse toDto(Category category) {
    return new CategoryResponse(category.getId(), category.getName());
  }
}
