package com.logbei.be.category.mapper;


import com.logbei.be.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.logbei.be.record.dto.*;
import java.util.List;
import com.logbei.be.category.dto.CategoryResponseDto;
import com.logbei.be.category.dto.CategoryPatchDto;
import com.logbei.be.category.dto.CategoryPostDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category categoryPostToCategory(CategoryPostDto post);
    @Mapping(target = "member.memberId", source = "memberId")
    Category categoryPatchToCategory(CategoryPatchDto patch);
    @Mapping(target = "memberId", source = "member.memberId")
    CategoryResponseDto categoryToCategoryResponse(Category category);
    List<CategoryResponseDto> categoriesToCategoryResponses(List<Category> categories);
}
