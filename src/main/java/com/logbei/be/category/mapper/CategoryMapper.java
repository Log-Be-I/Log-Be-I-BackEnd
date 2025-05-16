package com.logbei.be.category.mapper;

<<<<<<< HEAD:src/main/java/com/springboot/category/mapper/CategoryMapper.java
import com.springboot.category.dto.CategoryPatchDto;
import com.springboot.category.dto.CategoryPostDto;
import com.springboot.category.dto.CategoryResponseDto;
import com.springboot.category.entity.Category;
=======
import com.logbei.be.category.dto.CategoryDto;
import com.logbei.be.category.entity.Category;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/category/mapper/CategoryMapper.java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category categoryPostToCategory(CategoryPostDto post);
    @Mapping(target = "member.memberId", source = "memberId")
    Category categoryPatchToCategory(CategoryPatchDto patch);
    @Mapping(target = "memberId", source = "member.memberId")
    CategoryResponseDto categoryToCategoryResponse(Category category);
    List<CategoryResponseDto> categoriesToCategoryResponses(List<Category> categories);
}
