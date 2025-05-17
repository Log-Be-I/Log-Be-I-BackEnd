package com.logbei.be.category.service;

import com.logbei.be.category.entity.Category;
import com.logbei.be.category.repository.CategoryRepository;
import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.exception.ExceptionCode;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.member.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MemberService memberService;

    @InjectMocks
    private CategoryService categoryService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    @Test
    void createCategory_success() {
        // given
        Long memberId = 1L;
        Member member = TestDataFactory.createTestMemberWithDefaultCategories(memberId); // this includes 5 base categories
        Category category = new Category();
        category.setName("운동");
        category.setImage("image.png");

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        when(categoryRepository.existsByMemberAndName(member, "운동")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Category saved = categoryService.createCategory(category, memberId);

        // then
        assertEquals("운동", saved.getName());
        assertEquals(member, saved.getMember());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void createCategory_fail_dueToDuplicateName() {
        // given
        Long memberId = 1L;
        Member mockMember = TestDataFactory.createTestMemberWithDefaultCategories(memberId);
        Category mockCategory = TestDataFactory.createTestCategory("중복", mockMember);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(mockMember);
        when(categoryRepository.existsByMemberAndName(mockMember, "중복")).thenReturn(true);
        // when & then
        BusinessLogicException e = assertThrows(BusinessLogicException.class, () ->
                categoryService.createCategory(mockCategory, memberId));
        assertEquals(ExceptionCode.CATEGORY_EXISTS, e.getExceptionCode());
    }

    @Test
    void updateCategory_success() {
        // given
        Long memberId = 1L;
        Member member = TestDataFactory.createTestMemberWithDefaultCategories(memberId);
        Category existingCategory = TestDataFactory.createTestCategory("운동", member);
        existingCategory.setCategoryId(10L);
        Category updateRequest = new Category();
        updateRequest.setCategoryId(10L);
        updateRequest.setName("요가");
        updateRequest.setImage("newImage.png");

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByMemberAndName(member, "요가")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Category updated = categoryService.updateCategory(updateRequest, memberId);

        // then
        assertEquals("요가", updated.getName());
        assertEquals("newImage.png", updated.getImage());
        assertEquals(member, updated.getMember());
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    void findCategories_success() {
        // given
        Long memberId = 1L;
        Member member = TestDataFactory.createTestMemberWithDefaultCategories(memberId);
        List<Category> mockCategories = member.getCategories();

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        when(categoryRepository.findByMember_MemberId(memberId)).thenReturn(mockCategories);

        // when
        List<Category> result = categoryService.findCategories(memberId);

        // then
        assertEquals(mockCategories.size(), result.size());
        assertEquals(mockCategories, result);
        verify(memberService).findVerifiedExistsMember(memberId);
        verify(categoryRepository).findByMember_MemberId(memberId);
    }

    @Test
    void findVerifiedExistsCategory_success() {
        // given
        Long categoryId = 1L;
        Member member = TestDataFactory.createTestMemberWithDefaultCategories(1L);
        Category category = TestDataFactory.createTestCategory("운동", member);
        category.setCategoryId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        Category found = categoryService.findVerifiedExistsCategory(categoryId);

        // then
        assertEquals(categoryId, found.getCategoryId());
        assertEquals("운동", found.getName());
    }

    @Test
    void findVerifiedExistsCategory_zero_returnsDummyCategory() {
        // when
        Category dummy = categoryService.findVerifiedExistsCategory(0L);

        // then
        assertEquals(0L, dummy.getCategoryId());
        assertNull(dummy.getName());
    }

    @Test
    void findVerifiedExistsCategory_notFound_throwsException() {
        // given
        Long categoryId = 99L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // then
        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                categoryService.findVerifiedExistsCategory(categoryId));
        assertEquals(ExceptionCode.CATEGORY_NOT_FOUND, ex.getExceptionCode());
    }

    @Test
    void defaultCategory_throwsExceptionWhenDefault() {
        // given
        Category defaultCategory = new Category();
        defaultCategory.setDefault(true);

        // then
        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                categoryService.defaultCategory(defaultCategory));
        assertEquals(ExceptionCode.CANNAT_UPDATE_CATEGORY, ex.getExceptionCode());
    }

    @Test
    void verifyExistsCategory_throwsExceptionWhenDuplicate() {
        // given
        Member member = TestDataFactory.createTestMemberWithDefaultCategories(1L);
        String name = "중복";

        when(categoryRepository.existsByMemberAndName(member, name)).thenReturn(true);

        // then
        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                categoryService.verifyExistsCategory(member, name));
        assertEquals(ExceptionCode.CATEGORY_EXISTS, ex.getExceptionCode());
    }
}