package com.myce.expo.controller;

import com.myce.expo.dto.CategoryResponse;
import com.myce.expo.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private  final CategoryService categoryService;

  // 카테고리 목록 조회
  @GetMapping()
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    List<CategoryResponse> categories = categoryService.getCategories();
    return ResponseEntity.ok().body(categories);
  }
}
