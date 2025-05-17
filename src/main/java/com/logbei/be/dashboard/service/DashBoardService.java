package com.logbei.be.dashboard.service;

import com.logbei.be.dashboard.dto.DashBoardResponseDto;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.notice.service.NoticeService;
import com.logbei.be.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.springboot.dashboard.dto.UnansweredQuestion;
import java.util.List;
import com.springboot.dashboard.dto.RecentNotice;
@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final MemberService memberService;
    private final QuestionService questionService;
    private final NoticeService noticeService;

    public DashBoardResponseDto findVerifiedExistsDashBoard() {
        //오늘 회원가입한 회원의 이름
        List<String> newMemberNames = memberService.findTodayRegisteredMembers();
        //question 답변 미등록 수
        List<UnansweredQuestion> questions = questionService.findUnansweredQuestions();
        //새 공지 : title, 등록일자
        List<RecentNotice> recentNotices = noticeService.findTop5RecentNotices();
        return dashBoardToResponse(newMemberNames, questions, recentNotices);
    }

    public DashBoardResponseDto dashBoardToResponse (List<String> memberNames, List<UnansweredQuestion> questions,
                                                         List<RecentNotice> recentNotices){
        DashBoardResponseDto dto = new DashBoardResponseDto();
        dto.setTodayMemberCount(memberNames.size());
        dto.setTodayMemberNames(memberNames);
        dto.setQuestionTitles(questions);
        dto.setUnansweredQuestionCount(questions.size());
        dto.setRecentNotices(recentNotices);

        return dto;
    }
}
