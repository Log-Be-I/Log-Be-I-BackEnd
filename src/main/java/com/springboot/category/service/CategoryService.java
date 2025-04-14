package com.springboot.category.service;

import com.springboot.category.entity.Category;
import com.springboot.category.mapper.CategoryMapper;
import com.springboot.category.repository.CategoryRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final static String CATEGORY_DEFAULT_URL = "/categories";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final MemberService memberService;


    public Category createCategory(Category category, Long memberId) {
        //회원인지 확인
        Member findMember = memberService.validateExistingMember(memberId);

        //회원이 가진 카테고리 중 중복되는 이름이 있다면 예외처리
        if(categoryRepository.existsByMemberAndName(findMember, category.getName())){
            throw new BusinessLogicException(ExceptionCode.CATEGORY_EXISTS);
        }
        //저장
        return categoryRepository.save(category);
    }

    public Category updateCategory(Category category, Long memberId){
        //등록되어있는 카테고리 인지 확인
        Category findCategory = findVerifiedCategory(category.getCategoryId());
        //등록했던 작성자인지 확인
        AuthorizationUtils.isOwner(findCategory.getMember().getMemberId(), memberId);
        //해당 카테고리가 기본 제공 카테고리라면 수정되지 않아야 한다.
        defaultCategory(findCategory);

        Optional.ofNullable(category.getName())
                .ifPresent(name -> findCategory.setName(name));
        Optional.ofNullable(category.getImage())
                .ifPresent(image -> findCategory.setImage(image));

        return categoryRepository.save(findCategory);
    }

    //특정 회원의 카테고리 전체 조회
    public Page<Category> getCategories (int page, int size, long memberId) {
        memberService.validateExistingMember(memberId);

        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1이상이어야 합니다.");
        }
        //페이징 및 정렬 정보 생성
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("categoryId").descending());

        //특정 회원의 카테고리 조회
        return categoryRepository.findByMember_MemberId(memberId, pageable);
    }

    //카테고리 단일 조회
    public Category getCategory (Long categoryId, Long memberId) {
        Category findCategory = findVerifiedCategory(categoryId);
        //조회자가 작성자 or 관리자 인지 확인, 아니라면 예외처리
        AuthorizationUtils.isAdminOrOwner(findCategory.getCategoryId(), memberId);
        return findCategory;
    }

    //카테고리 삭제
    public void deleteCategory(Long categoryId, Long memberId) {
        Category findCategory = findVerifiedCategory(categoryId);
        //작성자 또는 관리자만 가능
        AuthorizationUtils.isAdminOrOwner(findCategory.getMember().getMemberId(), memberId);
        //기본 카테고리 삭제 불가
        defaultCategory(findCategory);
        //DB에서 삭제
        categoryRepository.delete(findCategory);
    }

    //이미 등록되어 있는지 검증
    public Category findVerifiedCategory(long categoryId){
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND));
    }

    //기본 카테고리는 수정, 삭제하지 못하게 하는 로직
    public void defaultCategory(Category category) {
        if(category.isDefault()) {
            //객체의 현재 상태에서 허용되지 않는 작업을 시도할 때 사용하는 예외처리
            throw new IllegalStateException("기본 카테고리는 수정할 수 없습니다.");
        }
    }
}
