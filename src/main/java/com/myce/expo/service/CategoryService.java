package com.myce.expo.service;

import com.myce.expo.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
  List<CategoryResponse> getCategories();
}
