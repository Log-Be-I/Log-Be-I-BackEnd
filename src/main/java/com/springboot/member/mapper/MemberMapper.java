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
    default MemberResponseDto memberToMemberResponseDto(Member member) {
        MemberResponseDto memberResponseDto = new MemberResponseDto();
        memberResponseDto.setName(member.getName());
        memberResponseDto.setEmail(member.getEmail());
        memberResponseDto.setProfile(member.getProfile());
        memberResponseDto.setBirth(member.getBirth());
        memberResponseDto.setRegion(member.getRegion());
        memberResponseDto.setNotification(member.isNotification());
        memberResponseDto.setNickname(member.getNickname());
        memberResponseDto.setMemberStatus(member.getMemberStatus());

        return memberResponseDto;
    }

    default List<MemberResponseDto> membersToMemberResponseDtos(List<Member> members) {
       List<MemberResponseDto> memberResponseDtoList =
        members.stream().map(member -> memberToMemberResponseDto(member)).collect(Collectors.toList());

       return memberResponseDtoList;
    }
}
