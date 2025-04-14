package com.springboot.category.mapper;

import com.springboot.category.dto.CategoryDto;
import com.springboot.category.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category categoryPostToCategory(CategoryDto.Post post);
    Category categoryPatchToCategory(CategoryDto.Patch patch);
    CategoryDto.Response categoryToCategoryResponse(Category category);
    List<CategoryDto.Response> categoriesToCategoryResponses(List<Category> categories);
}
