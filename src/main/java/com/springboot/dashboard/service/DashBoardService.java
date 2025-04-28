package com.springboot.dashboard.service;

import com.springboot.dashboard.dto.DashBoardResponseDto;
import com.springboot.member.service.MemberService;
import com.springboot.notice.service.NoticeService;
import com.springboot.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final MemberService memberService;
    private final QuestionService questionService;
    private final NoticeService noticeService;

    public DashBoardResponseDto findDashBoard() {
        //오늘 회원가입한 회원의 이름
        List<String> newMemberNames = memberService.findTodayRegisteredMembers();
        //question 답변 미등록 수
        List<DashBoardResponseDto.UnansweredQuestion> questions = questionService.findUnansweredQuestions();

        //새 공지 : title, 등록일자
        List<DashBoardResponseDto.RecentNotice> recentNotices = noticeService.findTop5RecentNotices();

        return dashBoardToResponse(newMemberNames, questions, recentNotices);

    }

    public DashBoardResponseDto dashBoardToResponse (List<String> memberNames, List<DashBoardResponseDto.UnansweredQuestion> questions,
                                                         List<DashBoardResponseDto.RecentNotice> recentNotices){
        DashBoardResponseDto dto = new DashBoardResponseDto();
        dto.setTodayMemberCount(memberNames.size());
        dto.setTodayMemberNames(memberNames);
        dto.setQuestionTitles(questions);
        dto.setUnansweredQuestionCount(questions.size());
        dto.setRecentNotices(recentNotices);

        return dto;
    }
}
