package com.springboot.member.mapper;

import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.dto.MemberPostDto;
import com.springboot.member.dto.MemberResponseDto;
import com.springboot.member.entity.Member;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    Member memberPostDtoToMember(MemberPostDto memberPostDto);
    Member memberPatchDtoToMember(MemberPatchDto memberPatchDto);
    MemberResponseDto memberToMemberResponseDto(Member member);

    default List<MemberResponseDto> membersToMemberResponseDtos(List<Member> members) {
       List<MemberResponseDto> memberResponseDtoList =
        members.stream().map(member -> memberToMemberResponseDto(member)).collect(Collectors.toList());

       return memberResponseDtoList;
    }
}
