package com.logbei.be.member.mapper;

import com.logbei.be.member.dto.MemberPatchDto;
import com.logbei.be.member.dto.MemberPostDto;
import com.logbei.be.member.dto.MemberResponseDto;
import com.logbei.be.member.entity.Member;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    Member memberPostDtoToMember(MemberPostDto memberPostDto);
    Member memberPatchDtoToMember(MemberPatchDto memberPatchDto);
    MemberResponseDto memberToMemberResponseDto(Member member);
    default List<MemberResponseDto> membersToMemberResponseDtos(List<Member> members) {
        List<MemberResponseDto> memberResponseDtoList =
                members.stream().map(member -> memberToMemberResponseDto(member))
                        .collect(Collectors.toList());
        return memberResponseDtoList;
    }
}
