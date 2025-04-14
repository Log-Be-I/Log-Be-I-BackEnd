package com.springboot.category.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.category.dto.CategoryDto;
import com.springboot.category.entity.Category;
import com.springboot.category.mapper.CategoryMapper;
import com.springboot.category.service.CategoryService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final static String CATEGORY_DEFAULT_URL = "/categories";
    private final CategoryService categoryService;
    private final CategoryMapper mapper;

    @PostMapping
    public ResponseEntity postCategory(@RequestBody CategoryDto.Post post,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        post.setMemberId(customPrincipal.getMemberId());
        Category category = categoryService.createCategory(mapper.categoryPostToCategory(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(CATEGORY_DEFAULT_URL, category.getCategoryId());

        return ResponseEntity.created(location).build();

    }

    @PatchMapping("/{category-id")
    public ResponseEntity patchCategory(@Positive @PathVariable("category-id") long categoryId,
                                        @RequestBody CategoryDto.Patch patch,
                                        @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        patch.setCategoryId(categoryId);
        patch.setMemberId(customPrincipal.getMemberId());
        Category updateCategory = categoryService.updateCategory(mapper.categoryPatchToCategory(patch), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.categoryToCategoryResponse(updateCategory)),
                HttpStatus.OK);
    }

    //특정 회원의 카테고리 목록 조회
    @GetMapping("/my")
    public ResponseEntity getMyCategory(@Positive @RequestParam("page") int page,
                                        @Positive @RequestParam("size") int size,
                                        @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Page<Category> categoryPage = categoryService.getCategories(page, size, customPrincipal.getMemberId());
        List<Category> categories = categoryPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(
                mapper.categoriesToCategoryResponses(categories), categoryPage), HttpStatus.OK
        );
    }

    @GetMapping("/{category-id}")
    public ResponseEntity getCategory(@Positive @PathVariable("category-id") Long categoryId,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Category getCategory = categoryService.getCategory(categoryId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.categoryToCategoryResponse(getCategory)), HttpStatus.OK
        );
    }

    @DeleteMapping("/{category-id}")
    public ResponseEntity deleteCategory(@Positive @PathVariable("category-id") Long categoryId,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        categoryService.deleteCategory(categoryId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
