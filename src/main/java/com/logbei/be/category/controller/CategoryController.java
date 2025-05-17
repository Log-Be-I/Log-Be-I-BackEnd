package com.logbei.be.category.controller;

import com.logbei.be.category.dto.CategoryPostDto;
import com.logbei.be.category.dto.CategoryPatchDto;
import com.logbei.be.category.dto.CategoryResponseDto;
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.category.dto.CategoryDto;
import com.logbei.be.category.entity.Category;
import com.logbei.be.category.mapper.CategoryMapper;
import com.logbei.be.category.service.CategoryService;
import com.logbei.be.responsedto.ListResponseDto;
import com.logbei.be.responsedto.SingleResponseDto;
import com.logbei.be.swagger.SwaggerErrorResponse;
import com.logbei.be.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "카테고리 API")
@Validated
public class CategoryController {
    private final static String CATEGORY_DEFAULT_URL = "/categories";
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    //swagger API - 등록
    @Operation(summary = "카테고리 새로 등록", description = "회원 또는 관리자가 새로운 카테고리를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 카테고리 등록",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"Access not allowed\"}"))),
            @ApiResponse(responseCode = "409", description = "찾을 수 없는 문의글",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"CATEGORY_NOT_FOUND.\"}")))
    })
    @PostMapping
    public ResponseEntity postCategory(@RequestBody CategoryPostDto categoryPostDto,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Category category = categoryService.createCategory(categoryMapper.categoryPostToCategory(categoryPostDto), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(CATEGORY_DEFAULT_URL, category.getCategoryId());

        return ResponseEntity.created(location).body(new SingleResponseDto<>(
                categoryMapper.categoryToCategoryResponse(category)));
    }
    //swagger API - 수정
    @Operation(summary = "카테고리 수정", description = "회원 또는 관리자가 카테고리를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 수정",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"기본 카테고리는 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"Access not allowed\"}")))
    })
    @PatchMapping("/{category-id}")
    public ResponseEntity patchCategory(@Positive @PathVariable("category-id") long categoryId,
                                        @RequestBody CategoryPatchDto categoryPatchDto,
                                        @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        categoryPatchDto.setCategoryId(categoryId);
        Category category =  categoryService.updateCategory(
                categoryMapper.categoryPatchToCategory(categoryPatchDto), customPrincipal.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(
                categoryMapper.categoryToCategoryResponse(category)), HttpStatus.OK);
    }

    //swagger API - 전체 조회
    @Operation(summary = "카테고리 전체 조회", description = "회원이 카테고리를 전체를 조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 전체 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"Access not allowed\"}")))
    })

    //특정 회원의 카테고리 목록 조회
    @GetMapping("/my")
    public ResponseEntity getMyCategory(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        List<Category> categoryList =  categoryService.findCategories(customPrincipal.getMemberId());
        return new ResponseEntity<>(new ListResponseDto<>(
                categoryMapper.categoriesToCategoryResponses(categoryList)), HttpStatus.OK
        );
    }

    //swagger API - 단일 조회
    @Operation(summary = "카테고리 단일 조회", description = "회원이 카테고리를 단일 조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 딘일 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"Access not allowed\"}")))
    })

    @GetMapping("/{category-id}")
    public ResponseEntity getCategory(@Positive @PathVariable("category-id") Long categoryId,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
       Category category = categoryService.findCategory(categoryId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(
                categoryMapper.categoryToCategoryResponse(category)), HttpStatus.OK);
    }

    //swagger API - 삭제
    @Operation(summary = "카테고리 삭제", description = "회원이 카테고리 하나를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "카테고리 삭제",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"NO_CONTENT\", \"message\": \"DELETED_DONE\"}"))),
            @ApiResponse(responseCode = "400", description = "기본 카테고리 삭제 불가",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"본 카테고리는 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"Access not allowed\"}")))
    })
    @DeleteMapping("/{category-id}")
    public ResponseEntity deleteCategory(@Positive @PathVariable("category-id") Long categoryId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        categoryService.deleteCategory(categoryId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
