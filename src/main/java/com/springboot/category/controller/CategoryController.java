package com.springboot.category.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.category.dto.CategoryDto;
import com.springboot.category.entity.Category;
import com.springboot.category.mapper.CategoryMapper;
import com.springboot.category.service.CategoryService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Category API", description = "카테고리 API")
public class CategoryController {
    private final static String CATEGORY_DEFAULT_URL = "/categories";
    private final CategoryService categoryService;
    private final CategoryMapper mapper;

    //swagger API - 등록
    @Operation(summary = "카테고리 새로 등록", description = "회원 또는 관리자가 새로운 카테고리를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 카테고리 등록"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "409", description = "이미 등록된 카테고리",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Conflict\", \"message\": \"CATEGORY_EXISTS.\"}")))
    })

    @PostMapping
    public ResponseEntity postCategory(@RequestBody CategoryDto.Post post,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Category category = categoryService.createCategory(mapper.categoryPostToCategory(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(CATEGORY_DEFAULT_URL, category.getCategoryId());

        return ResponseEntity.created(location).build();

    }

    //swagger API - 수정
    @Operation(summary = "카테고리 수정", description = "회원 또는 관리자가 카테고리를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 수정",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "기본 카테고리 삭제 불가",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\" : \"기본 카테고리는 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    @PatchMapping("/{category-id}")
    public ResponseEntity patchCategory(@Positive @PathVariable("category-id") long categoryId,
                                        @RequestBody CategoryDto.Patch patch,
                                        @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        patch.setCategoryId(categoryId);
        patch.setMemberId(customPrincipal.getMemberId());
        Category updateCategory = categoryService.updateCategory(mapper.categoryPatchToCategory(patch), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.categoryToCategoryResponse(updateCategory)),
                HttpStatus.OK);
    }

    //swagger API - 전체 조회
    @Operation(summary = "카테고리 전체 조회", description = "회원이 카테고리를 전체를 조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 전체 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

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

    //swagger API - 단일 조회
    @Operation(summary = "카테고리 단일 조회", description = "회원이 카테고리를 단일 조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 딘일 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    @GetMapping("/{category-id}")
    public ResponseEntity getCategory(@Positive @PathVariable("category-id") Long categoryId,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Category getCategory = categoryService.getCategory(categoryId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.categoryToCategoryResponse(getCategory)), HttpStatus.OK
        );
    }

    //swagger API - 삭제
    @Operation(summary = "카테고리 삭제", description = "회원이 카테고리를 전체를 조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 삭제"),
            @ApiResponse(responseCode = "400", description = "기본 카테고리 삭제 불가",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\" : \"기본 카테고리는 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    @DeleteMapping("/{category-id}")
    public ResponseEntity deleteCategory(@Positive @PathVariable("category-id") Long categoryId,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        categoryService.deleteCategory(categoryId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
