package com.springboot.ai.openai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.ai.openai.OpenAiProperties;
import com.springboot.ai.openai.dto.OpenAiMessage;
import com.springboot.ai.openai.dto.OpenAiRequest;
import com.springboot.ai.openai.dto.OpenAiResponse;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.record.entity.Record;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.entity.Report;
import com.springboot.report.service.ReportService;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {
    //api 키, baseUrl 등 설정 정보
    private final OpenAiProperties properties;
    //JSON 직렬화/역직렬화 도구
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReportService reportService;


    //최종 : List<Report> -> ReportService
    public List<Report> createReportsFromAi(List<ReportAnalysisRequest> requests) {
          return reportService.createReport(requests.stream()
                  .map(request -> generateReportFromAi(request))
                  .collect(Collectors.toList()));

    }

    //ReportAnalysisRequest -> JSON 문자열 -> aiRequest -> aiResponse. content -> Report
    public Report generateReportFromAi(ReportAnalysisRequest request){
        try {
            String recordJson = serializeRecords(request.getRecords());
            String prompt = reportTypeWeeklyOrMonthly(request, recordJson);
            OpenAiRequest aiRequest = buildChatRequest(prompt);
            OpenAiResponse aiResponse = sendToGpt(aiRequest);
            String content =  extractContent(aiResponse);
            return reportService.aiRequestToReport(request, content);

        } catch (IOException e) {
            log.error("GPT 분석 실패 - memberId: " + request.getMemberId(), e);
            throw new BusinessLogicException(ExceptionCode.REPORT_GENERATION_FAILED);
        }
    }

    //최종 조립 메서드 예시 : ReportAnalysisResponse 반환
    public String sendRecord(String prompt) throws IOException{

            // 사용자의 기록 리스트를 JSON 직렬화
//            String recordJson = serializeRecords(record);
            // 프롬프트 설정
//            String prompt = chatWithWeeklyPrompt(record);
            // GPT 요청 객체 생성
            OpenAiRequest chatRequest = buildChatRequest(prompt);
            // 실제 GPT 서버에 요청 보내고 응답 받기
            OpenAiResponse chatResponse = sendToGpt(chatRequest);
            // 응답 중 필요한 텍스트만 추출
            String content = extractContent(chatResponse);

        return content;
    }

    //기록 리스트 JSON 문자열로 반환
    public String serializeRecords(List<Record> records) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(records);
    }

    //ReportAnalysisRequest 의 ReportType = Weekly or Monthly 인 경우 타입에 맞는 prompt 반환
    public String reportTypeWeeklyOrMonthly(ReportAnalysisRequest request, String recordJson) {

        if(request.getReportType().equals(Report.ReportType.REPORT_WEEKLY)) {
            return chatWithWeeklyPrompt(recordJson);
        } else if(request.getReportType().equals(Report.ReportType.REPORT_MONTHLY)) {
            return chatWithMonthlyReport(recordJson);
        } else {
            throw new BusinessLogicException(ExceptionCode.INVALID_REPORT_TYPE);
        }

    }

    // JSON 을 역직렬화
    public Map<String, String> jsonToString (String json) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }


    // 단일 기록 JSON 문자열로 반환
//    public String serializeClovaText(String clovaText) throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.writeValueAsString(clovaText);
//    }

    //주간 프롬프트 - Json 문자열을 param으로 받음
    public String chatWithWeeklyPrompt(String recordJson) {

       return   "다음은 사용자의 일상 기록입니다. 이 내용을 분석하여 아래 항목을 각각 한 문단으로 분석해주세요.\n" +
                "각 항목 앞에는 큰따옴표로 키워드를 붙여주세요.\n" +
                "너무 긴 문장은 피하고, 적절한 길이(1~2줄)마다 줄바꿈을 넣어주세요.\n" +
                "출력은 사용자에게 읽기 편하도록 구성해야 하며, 프론트는 이 키워드들을 기준으로 파싱합니다.\n\n" +
                "출력 형식 예시는 다음과 같습니다:\n\n" +
                "\"요약\n" +
                "이번 주에는 산책과 독서 등 다양한 활동이 있었습니다.\n" +
                "기록은 총 10회였고, 그중 산책이 4회로 가장 많았습니다.\n\n" +
                "감정\n" +
                "전반적으로 긍정적인 감정이 많이 드러났습니다.\n" +
                "특히 사람들과의 만남이 감정을 끌어올리는 데 도움을 주었습니다.\n\n" +
                "인사이트\n" +
                "‘산책’, ‘운동’, ‘기분 좋음’이라는 키워드가 반복적으로 등장했습니다.\n" +
                "기록은 주로 아침 시간대에 집중되어 있습니다.\n\n" +
                "\"제안 다음 주에는 주말에도 가벼운 활동을 넣어 리듬을 유지해보세요.\"\n\n" +
                "<사용자 기록>\n" +
                "---\n" +
                recordJson + "\n" +
                "---";

    }

    //월간 프롬프트
    public String chatWithMonthlyReport(String recordJson) {
        return   "다음은 사용자의 한 달간의 일상 기록입니다. 이 내용을 분석하여 아래 항목을 각각 한 문단으로 작성해주세요.\n" +
                "각 항목 앞에는 큰따옴표로 키워드를 붙여주세요.\n" +
                "너무 긴 문장은 피하고, 1~2줄마다 줄바꿈을 넣어주세요.\n" +
                "출력은 사용자에게 읽기 편해야 하며, 프론트에서는 해당 키워드를 기준으로 내용을 파싱합니다.\n\n" +
                "출력 형식 예시는 다음과 같습니다:\n\n" +
                "\"요약\n" +
                "이번 달에는 운동, 독서, 모임 등 다양한 활동이 있었습니다.\n" +
                "기록은 총 28회였고, 독서가 가장 많이 기록되었습니다.\n\n" +
                "감정\n" +
                "긍정적인 감정이 다수를 차지했지만,\n" +
                "중간에 스트레스를 표현한 날도 일부 있었습니다.\n\n" +
                "인사이트\n" +
                "‘일찍 일어남’, ‘운동’, ‘계획 세움’ 같은 단어가 자주 반복되었습니다.\n" +
                "기록은 주로 오전과 평일에 몰려 있었습니다.\n\n" +
                "카테고리별 활동\n" +
                "일정 30%, 소비 25%, 건강 20%, 할일 25%로 나타났습니다.\n" +
                "일정과 소비 항목이 상대적으로 많았습니다.\n\n" +
                "패턴 분석\n" +
                "기록 시간대는 오전(9~11시)에 집중되었고,\n" +
                "요일별로는 화요일과 금요일에 활동이 많았습니다.\n\n" +
                "\"제안 다음 달에는 저녁 시간대에도 짧은 루틴을 만들어보세요.\"\n\n" +
                "<사용자 기록>\n" +
                "---\n" +
                recordJson + "\n" +
                "---";
    }

    // schedule 과 record 구분
    public String chatWithScheduleAndRecord(String clovaJson) {
        return "다음 문장을 읽고, \"record\" 또는 \"schedule\" 중 하나로 분류해 주세요.\n" +
                "그에 따라 정확히 아래 JSON 구조를 사용해서 응답해 주세요.\n" +
                "각 필드는 Java 서버의 DB 컬럼명과 일치해야 하며,\n" +
                "\"record\" 타입의 경우 categoryId는 아래 기준에 따라 Long 타입 정수로 설정하세요.\n\n" +
                "[ categoryId 기준표 ]\n" +
                "1: 일상 — 일반적인 감정, 경험, 일상의 회고\n" +
                "2: 소비 — 금전 소비나 지출에 대한 내용\n" +
                "3: 할 일 — \"정확한 시간 또는 날짜\"와 \"행위\"가 함께 포함된 계획 (예: 내일 책 읽기, 3시에 은행 가기)\n" +
                "4: 건강 — 운동, 식단, 수면 등 건강 관리 관련\n" +
                "5: 기타 — 위 분류에 해당하지 않는 경우\n\n" +
                "다른 카테고리에 속하지 못하고 지출과 관련된 내용이 있다면 소비로 분류한다\n" +
                "※ record → recordDateTime은 해당 기록이 발생한 시간입니다.\n" +
                "※ schedule → startDateTime은 사용자가 말한 일정의 시작 날짜 및 시간이며,\n" +
                "endDateTime은 그 일정의 종료 날짜 및 시간입니다.\n\n" +
                "날짜와 시간은 ISO 포맷을 사용하여 저장한다\n" +
                "※ 시작시간만 언급하고 종료시간을 언급하지 않았다면 마지막 시간은 해당 날의 23시59분59초까지로 설정한다.\n" +
                "※ 오늘, 내일과 같이 시간 내용 없이 특정 날만을 기록한다면 해당 날의 00시00분00초부터 23시:59분:59초로 설정한다.\n" +
                "※ recordDateTime 기준 미래의 내용이여야만 할 일 또는 schedule로 구분된다.\n" +
                "※ 소비와 schedule 둘다 성립할때는 할 일 또는 schedule로 구분한다.\n" +
                "※ 소비로 분류되는 기준은 명확히 지출이 있었는지 샀다, 구매했다, 지출했다 와 같은 단어들을 글을 통해 알 수 있어야한다.\n" +
                "※ 5번으로 분류되는 기준은 단순 메모와 같은 내용이어야한다. 예를 들면 노란색이 좋아, 준비물: 연필 과 같은 내용들만 5번으로 분류된다\n" +
                "※ 날짜와 시간 둘다 명시되어있지않지만 미래에 대한 계획 내용이라면 3번으로 분류한다.\n" +
                "※ 무언가를 한다는 행위 자체 또한 시간 또는 날짜가 포함되어있다면 schedule로 분류한다\n" +
                "※ 본인이 아니어도 스케쥴의 조건에 부합하는 내용이라면 schedule로 분류한다.\n" +
                "※ 정확한 날짜와 또는 시간이 명시되어있어도 행위에 대한 내용이 없다면 schedule로 분류하지않는다 \n" +
                "※ 시간이 명시되어있지않고 주말 이라는 단어로만 명시한다면 일요일로 설정하고 분석한다 \n" +
                "※ .\n" +
                "응답 형식은 아래 중 하나여야 합니다:\n\n" +
                "record:\n" +
                "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"content\": \"텍스트 내용\",\n" +
                "  \"recordDateTime\": \"2025-04-22T10:30:00\",\n" +
                "  \"categoryId\": 1\n" +
                "}\n\n" +
                "schedule:\n" +
                "{\n" +
                "  \"type\": \"schedule\",\n" +
                "  \"title\": \"회의\",\n" +
                "  \"startDateTime\": \"2025-04-22T14:00:00\",\n" +
                "  \"endDateTime\": \"2025-04-22T15:00:00\"\n" +
                "}\n\n" +
                "사용자 입력:\n" +
                "---\n" +
                clovaJson + "\n" +
                "---";
    }

//    GPT 서버에 요청보내기 (ChatRequest 생성)
    public OpenAiRequest buildChatRequest(String prompt) {
        //메세지 구성 : system + user prompt
        List<OpenAiMessage> messages = List.of(
                new OpenAiMessage("system", "You are a helpful assistant."),
                new OpenAiMessage("user", prompt)
        );

        OpenAiRequest request = new OpenAiRequest();
        request.setModel(properties.getModel());    //ai model
        request.setMessages(messages);
        request.setTemperature(request.getTemperature());
        return request;

    }

    // GPT 서버에 요청을 보내고 응답 content를 반환하는 메서드
    public OpenAiResponse sendToGpt(OpenAiRequest request) throws IOException {
        //GPT API에 보낼 POST 요청 생성
        HttpPost post = new HttpPost(properties.getBaseUrl());
        //요청 헤더 설정 : 인증토큰과 JSON 타입 명시
        post.setHeader("Authorization", "Bearer " + properties.getApiKey());
        post.setHeader("Content-Type", "application/json");
        //ChatRequest 객체를 JSON 문자열로 직렬화 -> 요청 본문에 담기
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8));

        //HTTP  클라이언트로 요청 전송 및 응답 수신
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {

            //응답 JSON 문자열 꺼냄
            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            //JSON 문자열을 ChatResponse 객체로 역직렬화
            return objectMapper.readValue(json, OpenAiResponse.class);
        }


    }

    //content만 꺼내서 파싱
    public String extractContent(OpenAiResponse response) {
        return response.getChoices().get(0).getMessage().getContent();
    }

}
