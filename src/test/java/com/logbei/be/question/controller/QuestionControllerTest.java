package com.logbei.be.question.controller;

import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.question.dto.QuestionPatchDto;
import com.logbei.be.question.dto.QuestionResponseDto;
import com.logbei.be.question.entity.Question;
import com.logbei.be.question.mapper.QuestionMapper;
import com.logbei.be.question.service.QuestionService;
import com.logbei.be.responsedto.MultiResponseDto;
import com.logbei.be.responsedto.SingleResponseDto;
import com.logbei.be.utils.UriCreator;
import com.springboot.question.dto.QuestionPostDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QuestionControllerTest {

    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionMapper questionMapper;
    @InjectMocks
    private QuestionController questionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // [1] postQuestion: 등록 요청 시 Created 상태와 Location 헤더, 반환 DTO 검증
    @Test
    void postQuestion_shouldReturnCreatedResponse() {
        // given
        QuestionPostDto postDto = new QuestionPostDto();
        Question entity = new Question();
        entity.setQuestionId(100L);
        QuestionResponseDto respDto = new QuestionResponseDto();

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(1L);
        when(questionMapper.questionPostToQuestion(postDto)).thenReturn(entity);
        when(questionService.createQuestion(entity, 1L)).thenReturn(entity);
        when(questionMapper.questionToQuestionResponse(entity)).thenReturn(respDto);

        // when
        ResponseEntity<SingleResponseDto<QuestionResponseDto>> response = questionController.postQuestion(postDto, principal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        URI expectedUri = UriCreator.createUri("/questions", 100L);
        assertThat(response.getHeaders().getLocation()).isEqualTo(expectedUri);
        assertThat(response.getBody().getData()).isSameAs(respDto);
    }

    // [2] patchQuestion: 수정 요청 시 OK 상태와 반환 DTO 검증
    @Test
    void patchQuestion_shouldReturnOkResponse() {
        // given
        long id = 200L;
        QuestionPatchDto patchDto = new QuestionPatchDto();
        Question entity = new Question();
        QuestionResponseDto respDto = new QuestionResponseDto();

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(2L);
        when(questionMapper.questionPatchToQuestion(patchDto)).thenReturn(entity);
        when(questionService.updateQuestion(entity, 2L)).thenReturn(entity);
        when(questionMapper.questionToQuestionResponse(entity)).thenReturn(respDto);

        // when
        ResponseEntity<SingleResponseDto<QuestionResponseDto>> response = questionController.patchQuestion(id, patchDto, principal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getData()).isSameAs(respDto);
    }

    // [3] getQuestions (admin): 관리자용 전체 조회 시 MultiResponseDto와 페이징 검증
    @Test
    void getQuestions_shouldReturnMultiResponseDto() {
        // given
        int page = 1, size = 5;
        String sortType = "newest";
        boolean onlyNotAnswer = false;
        String email = "e@example.com", title = "t";

        Question q = new Question();
        List<Question> list = List.of(q);
        Page<Question> pageResult = new PageImpl<>(list, PageRequest.of(page-1, size), list.size());
        QuestionResponseDto dto = new QuestionResponseDto();

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(3L);
        when(questionService.findQuestions(page, size, sortType, onlyNotAnswer, email, title)).thenReturn(pageResult);
        when(questionMapper.questionsToQuestionResponses(list)).thenReturn(List.of(dto));

        // when
        ResponseEntity<MultiResponseDto<QuestionResponseDto>> response = questionController.getQuestions(page, size, sortType, onlyNotAnswer, email, title, principal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        MultiResponseDto<QuestionResponseDto> body = response.getBody();
        assertThat(body.getData()).containsExactly(dto);
        assertThat(body.getPageInfo().getTotalElements()).isEqualTo(list.size());
    }

    // [4] getQuestion: 단건 조회 시 ResponseEntity와 DTO 검증
    @Test
    void getQuestion_shouldReturnSingleResponseDto() {
        // given
        long id = 300L;
        Question entity = new Question();
        QuestionResponseDto dto = new QuestionResponseDto();

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(5L);
        when(questionService.findQuestion(id, 5L)).thenReturn(entity);
        when(questionMapper.questionToQuestionResponse(entity)).thenReturn(dto);

        // when
        ResponseEntity<SingleResponseDto<QuestionResponseDto>> response = questionController.getQuestion(id, principal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getData()).isSameAs(dto);
    }

    // [6] deleteQuestion: 삭제 요청 시 No Content 상태 반환 검증
    @Test
    void deleteQuestion_shouldReturnNoContent() {
        // given
        long id = 400L;
        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(6L);
        doNothing().when(questionService).deleteQuestion(id, 6L);

        // when
        ResponseEntity<Void> response = questionController.deleteQuestion(id, principal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(questionService).deleteQuestion(id, 6L);
    }
}
