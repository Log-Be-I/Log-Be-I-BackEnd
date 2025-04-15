package com.springboot.category.mapper;

import com.springboot.category.dto.CategoryDto;
import com.springboot.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category categoryPostToCategory(CategoryDto.Post post);
    @Mapping(target = "member.memberId", source = "memberId")
    Category categoryPatchToCategory(CategoryDto.Patch patch);
    @Mapping(target = "memberId", source = "member.memberId")
    CategoryDto.Response categoryToCategoryResponse(Category category);
    List<CategoryDto.Response> categoriesToCategoryResponses(List<Category> categories);
}
