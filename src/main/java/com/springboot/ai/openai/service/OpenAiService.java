package com.springboot.ai.openai.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.springboot.ai.openai.OpenAiProperties;
import com.springboot.ai.openai.dto.OpenAiMessage;
import com.springboot.ai.openai.dto.OpenAiRequest;
import com.springboot.ai.openai.dto.OpenAiResponse;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.log.LogStorageService;
import com.springboot.report.dto.RecordForAnalysisDto;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.dto.ReportAnalysisResponse;
import com.springboot.report.entity.Report;
import com.springboot.report.service.ReportService;
import com.springboot.utils.ReportUtil;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {
    //api 키, baseUrl 등 설정 정보
    private final OpenAiProperties properties;
    //JSON 직렬화-역직렬화 도구
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReportService reportService;
    private final LogStorageService logStorageService;
    private static final int TIMEOUT_MILLIS = 60 * 1000;    //60초
    private static final int MAX_RETRY_COUNT = 3;
    String logNameReport = "GPT_Report";
    String logNameRecord = "GPT_Record";
    //Report
    //GPT한테 분석 정보(사용자의 기록)를 보내고 분석 받은 데이터를 List<Report>로 받음
    public List<Report> createReportsFromAiInBatch(List<ReportAnalysisRequest> requests) {
        //GPT 분석 요청은 10명씩 끊어서 전달 : 토큰 절약 + 응답 지연 방지
        List<List<ReportAnalysisRequest>> batches = ReportUtil.partitionList(requests, 10);
        //분석 정보를 반환 받을 빈객체 생성
        List<Report> allReports = new ArrayList<>();

        //배치별(사용자 10명 단위)로 GPT에 요청
        //List<ReportAnalysusRequest> -> List<ReportAnalysisResponse> -> List<Report>
        for (List<ReportAnalysisRequest> batch : batches) {
            List<Report> batchReports = processBatchWithGpt(batch);
            allReports.addAll(batchReports);
        }

        // 3. DB에 저장
        return reportService.analysisResponseToReportList(allReports);
    }

    // GPT 호출 처리 (단일 배치)
    private List<Report> processBatchWithGpt(List<ReportAnalysisRequest> requests) {

        return requests.stream()
                //List<ReportAnalysisRequest> -> List<ReportAnalysisResponse>
                .map(request -> generateReportFromAi(request))
                //List<ReportAnalysisResponse> -> List<Report>
                .map(reportService::analysisResponseToReport)
                .collect(Collectors.toList());
    }

    //Report
    //ReportAnalysisRequest -> JSON 문자열 -> aiRequest -> aiResponse. content -> Report
    public ReportAnalysisResponse generateReportFromAi(ReportAnalysisRequest request){
//        request.getRecords().forEach(record -> record.setMember(new Member()));
        try {
            //JSON 직렬화
            String recordJson = serializeRecords(request.getAnalysisDtoList());
            //prompt 구성
            String prompt = reportTypeWeeklyOrMonthly(request, recordJson);
            //OpenAI 요청
            OpenAiRequest aiRequest = buildChatRequest(prompt);
            //GPT한테 요청 보내고 응답 받음
            OpenAiResponse aiResponse = sendToGpt(aiRequest);
            //content 추출 & JSON 파싱 전 줄바꿈 이스케이프 처리
            String content = extractContent(aiResponse);
            // JSON -> Map
            Map<String, String> contentMap = jsonToMap(content); //aiResponse = {OpenAiResponse@16415}
            // generateReportFromAi 내부
            logStorageService.logAndStoreWithError("contentMap parse success - memberId: {}, contentMap keys: {}", logNameReport,request.getMemberId(), contentMap.keySet());
            //결과 매핑
            return reportService.aiRequestToResponse(request, contentMap);

        } catch (IOException e) {
            logStorageService.logAndStoreWithError("GPT 분석 실패 - memberId: {}", logNameReport, request.getMemberId(), e);
            throw new BusinessLogicException(ExceptionCode.REPORT_GENERATION_FAILED);
        }
    }

    //audio-Record 최종
    // 문장을 우리가 원하는 key value 형태로 변환
    public Map<String, String> createRecordOrSchedule(String text) throws IOException {
        // 한국 시간대 기준으로 현재 시간 가져오기
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDateTime nowKST = LocalDateTime.now(seoulZone);  // ← 이렇게 해야 정확히 KST
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(text, new TypeReference<>() {
        });
        try {
            String value = map.get("text");
            // 사용자 입력 text -> JSON 으로 변경
            String prompt = chatWithScheduleAndRecord(value, nowKST.toString());
            // GPT 요청 객체 생성
            OpenAiRequest chatRequest = buildChatRequest(prompt);
            // 실제 GPT 서버에 요청 보내고 응답 받기
            OpenAiResponse chatResponse = sendToGpt(chatRequest);
            // 응답 중 필요한 텍스트만 추출
            String content = extractContent(chatResponse);
            logStorageService.logAndStoreWithError("GPT_audio_record process: {}", logNameRecord, content);
            // json 역직렬화 (JSON -> Map)
            return jsonToMap(content);
        }
        catch (IOException e) {
            logStorageService.logAndStoreWithError("createRecordOrSchedule Failed - reason: {}", logNameRecord, e.getMessage(), e);

//            logStorageService.logAndStoreWithError("GPT_audio_record Failed", "GPT", e.getMessage());
            throw new BusinessLogicException(ExceptionCode.REPORT_GENERATION_FAILED); // 너가 따로 정의한 예외 던짐
        }
    }

    //기록 리스트 JSON 문자열로 직렬화 (객체 -> JSON)
    public String serializeRecords(List<RecordForAnalysisDto> dtos) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.writeValueAsString(dtos);
    }

    // JSON 을 역직렬화 (JSON -> 객체)
    public Map<String, String> jsonToMap(String json) throws IOException{
        // 마크다운 코드 블럭 제거
        if (json.startsWith("```")) {
            json = json.replaceAll("```json", "").replaceAll("```", "").trim();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        //줄바꿈 문자(CTRL 문자) 허용 설정
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    //ReportAnalysisRequest 의 ReportType = Weekly or Monthly 인 경우 타입에 맞는 prompt 반환
    public String reportTypeWeeklyOrMonthly(ReportAnalysisRequest request, String recordJson) {

        if(request.getReportType().equals(Report.ReportType.REPORT_WEEKLY)) {
            return chatWithWeeklyPrompt(recordJson);
        } else if(request.getReportType().equals(Report.ReportType.REPORT_MONTHLY)) {
            return chatWithMonthlyPrompt(recordJson);
        } else {
            throw new BusinessLogicException(ExceptionCode.INVALID_REPORT_TYPE);
        }
    }

    //Weekly Report Prompt
    public String chatWithWeeklyPrompt(String recordJson) {

        return  "다음은 사용자의 한 주간 기록 데이터야. 이를 기반으로 다음 6가지 항목을 분석해야해.\n" +
                "출력은 사용자가 읽기 편하게 작성하며, 각 문장은 1~2줄마다 줄바꿈을 해야해.\n" +
                "분석 결과는 Map<String, String> 형태로 저장해줘.\n\n" +

                "[summary]\n" +
                "6개의 기록 분류(일상, 소비, 할 일, 건강, 메모, 일정) 중에서\n" +
                "가장 많이 기록된 분류와 총 횟수, 그 안에서 가장 많이 수행된 활동 하나를 알려줘.\n" +
                "형식: 가장 많이 기록된 Category [건강], 총 횟수 [25], 주요 활동 [스트레칭]\n\n" +

                "[emotionRatio]\n" +
                "- 감정 표현은 기쁨, 행복, 쾌활, 편안, 슬픔, 불만, 버럭, 불안 중 최소 3개~최대 5개 선택\n" +
                "- 각 감정은 0~10 점수로 표현\n" +
                "- 마지막에 긍정/중립/부정 비율을 백분율로 표시 (합계 100%)\n" +
                "형식 예시:\n" +
                "기쁨 : 6, 불만 : 4, 편안 : 7\n" +
                "[긍정 : 60%, 중립 : 30%, 부정 : 10%]\n\n" +

                "[insight]\n" +
                "- 자주 사용한 단어(1~3개), 반복된 키워드(1~3개) 각각 알려줘\n" +
                "- 자주 사용한 단어는 일상어 위주로 하되, 오늘, 어제, 내일, 아침, 점심, 저녁 등\n" +
                "날짜나 시간대를 단순히 지칭하는 단어는 제외해줘.\n" +
                "- 반복된 키워드는 사용자의 행동 습관 또는 관심 주제 중심으로 분석해줘\n" +
                "형식:\n" +
                "자주 사용한 단어 : 진짜, 너무, 음\n" +
                "반복된 키워드 : 저녁 산책, 친구 통화, 출근길 커피\n\n" +

                "[suggestion]\n" +
                "- 한 달간 리듬이 깨졌던 요일이나 이상 패턴을 분석\n" +
                "- 다음 달에 도움이 될 제안을 1~2문장 작성\n" +
                "형식:\n" +
                "수요일 저녁에 집중력이 자주 낮아졌습니다.\n" +
                "루틴을 조정해보거나 짧은 산책을 넣어보는 건 어때?\"\n\n" +

                "분석 결과는 반드시 아래와 같은 JSON(key-value) 구조로, 코드블럭(```json) 없이 출력하세요.\n" +
                "value는 사람이 읽기 좋은 문장입니다.\n" +
                "value에 여러 문장이 필요하면 반드시 \n(이스케이프 문자)로 줄바꿈하세요.\n" +
                "절대 실제 줄바꿈은 하지 마세요.\n" +
                "각 항목의 key는 \n " +
                " - summary : \n" +
                " - emotionRatio : \n" +
                " - insight : \n" +
                " - suggestion : \n" +
                "입니다. \n\n" +

                "JSON 이외의 설명, 예시, 코드블럭, 주석, Map 선언 등은 절대 포함하지 말아줘.\n" +
                "각 항목은 구체적이고 자연스러운 문장으로 2~4문장 정도로 표현하되, 생성되는 JSON 결과는 총 길이가 최소 200자 이상, 최대 1000자 미만이 되도록 작성해줘.\n" +
                "반드시 한 줄짜리 JSON 문자열로 출력해줘. 줄바꿈(\n), 들여쓰기, 공백 없이 출력해줘.\n" +
                "출력 형식 아래와 같습니다:\n\n" +
                "예시 : \n" +
                "{\n"
                + "\"summary\": \"가장 많이 기록된 Category는 [건강]이고, 총 25회 기록되었습니다. 주요 활동은 스트레칭 입니다.\n"
                + "\"emotionRatio\": \"기쁨: 6, 불안: 4, 편안: 7 [긍정: 60%, 중립: 30%, 부정: 10%]\n"
                + "\"insight\": \"자주 사용한 단어: 진짜, 너무, 음, 반복된 키워드: 저녁 산책, 친구 통화, 출근길 커피\n"
                + "\"suggestion\": \"수요일 저녁에 집중력이 자주 낮아졌습니다. 루틴을 조정해보거나 짧은 산책을 넣어보는 건 어때요?\n"
                + "}\n" +

                "<사용자 기록>\n" +
                "----\n" +
                recordJson + "\n" +
                "----";
    }

    //Monthly Report Prompt
    public String chatWithMonthlyPrompt(String recordJson) {
        return "다음은 사용자의 한 달간 기록 데이터야. 이를 기반으로 다음 6가지 항목을 분석해야해.\n" +
                "출력은 사용자가 읽기 편하게 작성하며, 각 문장은 1~2줄마다 줄바꿈을 해야해.\n" +
                "분석 결과는 Map<String, String> 형태로 저장해줘.\n\n" +

                "[summary]\n" +
                "6개의 기록 분류(일상, 소비, 할 일, 건강, 메모, 일정) 중에서\n" +
                "가장 많이 기록된 분류와 총 횟수, 그 안에서 가장 많이 수행된 활동 하나를 알려줘.\n" +
                "형식: 가장 많이 기록된 Category [건강], 총 횟수 [25], 주요 활동 [스트레칭]\n\n" +

                "[emotionRatio]\n" +
                "- 감정 표현은 기쁨, 행복, 쾌활, 편안, 슬픔, 불만, 버럭, 불안 중 최소 3개~최대 5개 선택\n" +
                "- 각 감정은 0~10 점수로 표현\n" +
                "- 마지막에 긍정/중립/부정 비율을 백분율로 표시 (합계 100%)\n" +
                "형식 예시:\n" +
                "기쁨 : 6, 불만 : 4, 편안 : 7\n" +
                "[긍정 : 60%, 중립 : 30%, 부정 : 10%]\n\n" +

                "[insight]\n" +
                "- 자주 사용한 단어(1~3개), 반복된 키워드(1~3개) 각각 알려줘\n" +
                "- 자주 사용한 단어는 일상어 위주로 하되, 오늘, 어제, 내일, 아침, 점심, 저녁 등\n" +
                "날짜나 시간대를 단순히 지칭하는 단어는 제외해줘.\n" +
                "- 반복된 키워드는 사용자의 행동 습관 또는 관심 주제 중심으로 분석해줘\n" +
                "형식:\n" +
                "자주 사용한 단어 : 진짜, 너무, 음\n" +
                "반복된 키워드 : 저녁 산책, 친구 통화, 출근길 커피\n\n" +

                "[suggestion]\n" +
                "- 한 달간 리듬이 깨졌던 요일이나 이상 패턴을 분석\n" +
                "- 다음 달에 도움이 될 제안을 1~2문장 작성\n" +
                "형식:\n" +
                "수요일 저녁에 집중력이 자주 낮아졌습니다.\n" +
                "루틴을 조정해보거나 짧은 산책을 넣어보는 건 어때?\"\n\n" +

                "[categoryStat]\n" +
                "- 5개 카테고리(일상, 소비, 할 일, 건강, 메모)별 활동 비율을 백분율로 표현\n" +
                "- 총합 100%, 해석도 함께 제공\n" +
                "형식:\n" +
                "일정 30%, 소비 25%, 건강 20%, 할일 25%로 나타났습니다.\n" +
                "일정과 소비 항목이 상대적으로 많았습니다.\n\n" +

                "[pattern]\n" +
                "- 활동이 집중된 시간대(예: 오전/오후), 요일별 기록량 패턴을 분석\n" +
                "- 제안도 함께 작성\n" +
                "형식:\n" +
                "기록 시간대는 오전(9~11시)에 집중되었고,\n" +
                "요일별로는 화요일과 금요일에 활동이 많았습니다.\n" +
                "다음 달에는 저녁 시간대에도 짧은 루틴을 만들어보세요.\n\n" +

                "분석 결과는 반드시 아래와 같은 JSON(key-value) 구조로, 코드블럭(```json) 없이 출력하세요.\n" +
                "value는 사람이 읽기 좋은 문장입니다.\n" +
                "value에 여러 문장이 필요하면 반드시 \n(이스케이프 문자)로 줄바꿈하세요.\n" +
                "절대 실제 줄바꿈은 하지 마세요.\n" +
                "각 항목의 key는 \n " +
                " - summary : \n" +
                " - emotionRatio : \n" +
                " - insight : \n" +
                " - suggestion : \n" +
                " - categoryStat : \n" +
                " - pattern : \n" +
                "입니다. \n\n" +

                "JSON 이외의 설명, 예시, 코드블럭, 주석, Map 선언 등은 절대 포함하지 말아줘.\n" +
                "각 항목은 구체적이고 자연스러운 문장으로 2~4문장 정도로 표현하되, 생성되는 JSON 결과는 총 길이가 최소 200자 이상, 최대 1000자 미만이 되도록 작성해줘.\n" +
                "반드시 한 줄짜리 JSON 문자열로 출력해줘. 줄바꿈(\n), 들여쓰기, 공백 없이 출력해줘.\n" +
                "출력 형식 아래와 같습니다:\n\n" +
                "예시 : \n" +
                "{\n"
                + "\"summary\": \"가장 많이 기록된 Category는 [건강]이고, 총 25회 기록되었습니다. 주요 활동은 스트레칭 입니다.\n"
                + "\"emotionRatio\": \"기쁨: 6, 불안: 4, 편안: 7 [긍정: 60%, 중립: 30%, 부정: 10%]\n"
                + "\"insight\": \"자주 사용한 단어: 진짜, 너무, 음, 반복된 키워드: 저녁 산책, 친구 통화, 출근길 커피\n"
                + "\"suggestion\": \"수요일 저녁에 집중력이 자주 낮아졌습니다. 루틴을 조정해보거나 짧은 산책을 넣어보는 건 어때요?\n"
                + "\"categoryStat\": \"일정 30%, 소비 25%, 건강 20%, 할일 25%로 나타났습니다. 일정과 소비 항목이 상대적으로 많았습니다.\n"
                + "\"pattern\": \"기록 시간대는 오전(9~11시)에 집중되었고, 요일별로는 화요일과 금요일에 활동이 많았습니다. 다음 주에는 저녁 시간대에도 짧은 루틴을 만들어보세요.\n"
                + "}\n" +

                "<사용자 기록>\n" +
                "----\n" +
                recordJson + "\n" +
                "----";
    }

    // schedule or record 생성 Prompt
    public String chatWithScheduleAndRecord(String clovaJson, String time) {
        return "- 너는 다양한 사람들의 일기, 생활 기록, 메모 등을 분석하여 그 내용을 정확히 분류하는 **빅데이터 전문가야**\n" +
                "- " + clovaJson + "을 읽고 분석하여 1차 분류 이후, 분류된 항목에 맞는 2차 분류 기준에 따라 최종 분류하여 값을  3차 반환 기준이 안내하는 형태에 맞춰 최종 데이터를 반환한다.\n" +
                "- base 기준은 모든 분류 기준 및 3차 반환에 적용되며 가장 우선적으로 적용되어야한다.\n" +
                "- 모든 시간 관련 기록들은 KST 를 기준으로 작성하되 +09:00은 빼고 출력한다.이외에 모든 시간 관련된 데이터를 분석 및 출력하기전에 시간 데이터 분류 기준을 1순위로 참고하여 작성한다.\n" +
                "- 입력 텍스트는 아래 순서에 따라 판단합니다:\n" +
                "    1. **할 일 (record, categoryId = 3)**\n" +
                "    2. **소비 (record, categoryId = 2)**\n" +
                "    3. **건강 (record, categoryId = 4)**\n" +
                "    4. **일상 (record, categoryId = 1)**\n" +
                "    5. **기타 (record, categoryId = 5)**\n" +
                "---\n" +
                "**base 기준 :**\n" +
                "- 최종 반환 데이터는 JSON 으로 작성하여 반환한다.\n" +
                "- 1번 및 2번 반환형태 를 JSON으로 변환한 데이터를 제외한 다른 문장, 단어, 등 일체 부가 설명은 응답 데이터에 포함시키않고 출력도 금지한다.\n" +
                "---\n" +
                "**1차 분류 :**\n" +
                "- input text를 읽고  1차 분류 기준에 맞게 “record” 와 “schedule” 중 하나로 구분해야한다.\n" +
                "- 각 필드는 Java Sever의 DB에 저장된 Colum name 과 동일해야한다.\n" +
                "---\n" +
                "**1차 분류 기준 :**\n" +
                "- 분류 중 “schedule”은 앱 내에 있는 “calendar”에 작성되는 내용으로 명확한 시간/날짜 + 특정 행위/행동 을 포함한 내용만 분류한다. 이외 데이터들은 “record”로 분류하여 “2차 분류_record”에 따라 재분류된다.\n" +
                "---\n" +
                "**2차 분류_record :**\n" +
                "- 2차 분류 기준_record 를 참고하여 제시된 글의 카테고리를 구분해야한다.\n" +
                "- 문장의 내용이 하나의 카테고리를 특정하여 분류하기 애매하다면 “2차 분류 기준 참고 사항” 을 따른다.\n" +
                "- categoryId는 아래 기준에 따라 Long 타입 정수로 설정한다.\n" +
                "- 정확한 날짜/시간이 없더라도 오늘, 내일, 주말, 아침, 저녁, 점심 등 특정 기간 및 시간을 특정할 수 있다면 이는 불명확한 내용이 아니며, 이를 제외한 다른 문장 및 단어를 파악하여 분류하여야한다\n" +
                "---\n" +
                "**2차 분류 기준_record :**\n" +
                "- “일상” 분류 기준\n" +
                "    - 감정, 경험 일상의 회고 등\n" +
                "    - 어느 카테고리에도 분류되지 않은 일상 기록으로 판된되는 것들은 일상 카테고리로 분류한다\n" +
                "    - 일상으로도 분류가 어렵다면 그때 마지막 순위인 기타 카테고리 검증 단계로 넘긴다\n" +
                "- “소비” 분류 기준\n" +
                "    - 금전 소비, 지출 관련 내용\n" +
                "    - 명확한 소비 계획이 입력되더라도 날짜와 시간이 명확한 미래의 계획이라면 “소비”로 분류하지 않는다.\n" +
                "    - 입력된 시간 기준 과거의 소비 내용만 “소비”카테고리로 분류한다.\n" +
                "- “할 일” 분류 기준\n" +
                "    - 불명확한 날짜/시간 + 특정 행위 (예: 내일 책 읽기)\n" +
                "    - 정확한 시간과 날짜가 없더라도 오늘, 내일, 주말 과 같은 특정 날짜를 지칭하는 단어가 들어가있다면 할 일 카테고리고 구분한다.\n" +
                "    - ~었어, ~했었어 와 같은 과거형은 할 일 로 구분될 수 없으며 할 일 카테고리를 제외시키고 다시 재분류한다.\n" +
                "- “건강” 분류 기준\n" +
                "    - 운동, 식단, 수면 등 건강 관련 내용\n" +
                "- “기타” 분류 기준\n" +
                "    - 짧은 메모 불명확한 문장 (예: “노란색이 좋아”)\n" +
                "    - “기타” 카테고리는 가장 최하단 분류 카테고리이며, 어떠한 카테고리에도 속하지 못하는 데이터는 “기타” 카테고리를 할당한다.\n" +
                "---\n" +
                "**2차 분류_schedule :**\n" +
                "- 2차 분류 기준_schedule 를 참고하여 제시된 글의 시간과 목적을 작성해야한다.\n" +
                "---\n" +
                "**2차 분류 기준_schedule:**\n" +
                "- 명확한 날짜/시간 + 특정 행위 (예: 내일 책 읽기)\n" +
                "- 시작 날짜/시간 ~ 종료 날짜/시간 + 특정 행위 (예: 내일 책 읽기)\n" +
                "---\n" +
                "**2차 분류 기준 참고 사항 :**\n" +
                "- LocalDateTime.now 한국시간 기준 입력된 지출 내용이 현재 보다 과거의 일이라면 “소비” 카테고리에 분류하고, 현재 또는 미래에 발생할 소비로 판단되면 “schedule”로 분류한다.\n" +
                "- 날짜/시간이 아닌 “오늘”, “주말” 과 같은 단어들은 “시간 데이터 분류 기준” 항목 기준에 따른다\n" +
                "---\n" +
                "**시간 데이터 분류 기준:**\n" +
                "시간 관련 기준은 모든 시간은 KST 기준 ISO 8601 Local Date-Time 형식 사용하며 한국 시간을 기준으로한다:\n" +
                "[recordDateTime 설정 기준]\n" +
                "- 모든 \"record\" 타입 데이터의 \"recordDateTime\"은 문장 내 날짜/시간 표현과 **무관하게**, 반드시 현재 시점(LocalDateTime.now())을 기준으로 작성해야 한다.\n" +
                "- 예: \"저번달에 뭐 샀다\", \"오늘 뭐 했다\" 같은 문장이더라도 **항상 현재 시점 (LocalDateTime.now())을 기준으로, 한국 시간(KST) 기준 ISO 8601 포맷(yyyy-MM-ddTHH:mm:ss) 값**을 \\\"recordDateTime\\\"에 설정해야 한다.\\n" +
                "- 절대로 문장 내 날짜(예: 3월 7일)를 기준으로 \"recordDateTime\"을 역산하지 않는다.\n" +
                "- 기준 날짜는 LocalDateTime.now() 기준으로" + time + "이며," + "해당 날짜를 기준으로 상대 시간 표현을 해석해야 한다.\n" +
                "- \"다음달 X일\" → 현재 날짜 기준 다음 달의 X일로 설정한다. 예: 오늘이 4월이라면, \"다음달 11일\"은 5월 11일로 인식한다.\n" +
                "---\n" +
                "- \"schedule\"의 \"startDateTime\"과 \"endDateTime\"은 사용자의 발화에서 유추된 일정의 시간입니다.\n" +
                "- \"오늘\" → 당일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"어제\" → 현재 시간 기준 -1일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"그제\", \"그저께\" → 현재 시간 기준 -2일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"내일\" → 현재 시간 기준 +1일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"모레\" → 현재 시간 기준 +2일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"글피\" → 현재 시간 기준 +3일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"내후일\" → 현재 시간 기준 +4일 00:00:00 ~ 23:59:59 설정\n" +
                "- \"이번주\" → 현재 날짜가 포함된 주의 월요일 00:00:00 ~ 일요일 23:59:59 설정\n" +
                "- \"다음주\" → 현재 날짜 기준 다음 주 월요일 00:00:00 ~ 일요일 23:59:59 설정\n" +
                "- \"다다음주\" → 현재 날짜 기준 다다음 주 월요일 00:00:00 ~ 일요일 23:59:59 설정\n" +
                "- \"이번달\" → 현재 날짜가 포함된 월의 1일 00:00:00 ~ 말일 23:59:59 설정\n" +
                "- \"다음달\" → 현재 날짜 기준 다음 달 1일 00:00:00 ~ 말일 23:59:59 설정\n" +
                "- \"주말\" → 가장 가까운 토요일 00:00:00 ~ 일요일 23:59:59 설정\n" +
                "- \"저번달\", \"지난달\" → 현재 날짜 기준 -1개월 1일 00:00:00 ~ 말일 23:59:59 설정\n" +
                "- \"저번주\", \"지난주\", \"전 주\" → 현재 날짜 기준 -1주 월요일 00:00:00 ~ 일요일 23:59:59 설정\n" +
                "- \"금일\" → 현재 날짜 00:00:00 ~ 23:59:59 설정 (오늘과 동일)\n" +
                "- \"익일\", \"차일\", \"명일\" → 현재 날짜 기준 +1일 00:00:00 ~ 23:59:59 설정 (내일과 동일)\n" +
                "- \"작일\" → 현재 날짜 기준 -1일 00:00:00 ~ 23:59:59 설정 (어제와 동일)\n" +
                "- 종료 시간이 언급되지 않으면 해당 날짜의 23:59:59로 자동 설정\n" +
                "- 정확한 시간은 반드시 ISO 8601 포맷(`yyyy-MM-ddTHH:mm:ss`)으로 변환\n" +
                "---\n" +
                "**3차 반환 :**\n" +
                "- 최종 데이터 반환으로 “3차 반환 기준”의 내용을 준수하며 응답데이터를 반환한다\n" +
                "\n" +
                "**3차 반환 기준 :**\n" +
                "- 데이터의 반환 형태는 JSON 형태로 줘야하며, JSON 데이터 이외에 다른 설명 및 문장들은 응답 데이터에 포함시키지 않는다.\n" +
                "- 만약 2차 분류 기준을 “record” 기준에 따랐다면 1번의 형태로 반환되며, “schedule” 기준에 따랐다면 2번의 형태로 반환된다.\n" +
                "- 입력되는 텍스트는 한국어로 입력되며 분석 결과는 반드시 JSON 형식으로만 출력되어야 한다.\n" +
                "---\n" +
                "1번 반환 형태_”record”:\n" +
                "- 각 컬럼에 작성될 데이터의 기준은 “1번 반환 형태 기준” 을 참고하여 아래의 예시 형태처럼 작성되어야 한다.\n" +
                "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"content\": \"텍스트 내용\",\n" +
                "  \"recordDateTime\": \"2025-04-22T14:00:00\",\n" +
                "  \"categoryId\": 1\n" +
                "}\n" +
                "---\n" +
                "1번 반환 형태 기준:\n" +
                "- \"type\" ⇒ 2차 분류기준 “record”로 분류되었다면 “record”로 작성된다\n" +
                "- \"content\" ⇒ 입력된 텍스트 본문이 입력되어야 한다\n" +
                "- \"recordDateTime\" ⇒  LocalDateTime.now() 로 적용한다 이때 시간은 한국시간 기준으로 작성되어야 한다.\n" +
                "- \"categoryId\" ⇒ 아래에 정리된 categoryId를 참고하여 분류된 카테고리의 id 값을 Long 타입 숫자로 작성해준다.\n" +
                "    - “일상” categoryId = 1\n" +
                "    - “소비” categoryId = 2\n" +
                "    - “할 일” categoryId = 3\n" +
                "    - “건강” categoryId = 4\n" +
                "    - “기타” categoryId = 5\n" +
                "---\n" +
                "2번 반환 형태_”schedule”:\n" +
                "- 각 컬럼에 작성될 데이터의 기준은 “2번 반환 형태 기준” 을 참고하여 아래의 예시 형태처럼 작성되어야 한다.\n" +
                "{\n" +
                "  \"type\": \"schedule\",\n" +
                "  \"title\": \"텍스트 내용\",\n" +
                "  \"startDateTime\": \"2025-04-22T14:00:00\",\n" +
                "  \"endDateTime\": \"2025-04-22T14:00:00\"\n" +
                "}\n" +
                "---\n" +
                "2번 반환 형태 기준:\n" +
                "- \"type\" ⇒ 2차 분류기준 “schedule”로 분류되었다면 “schedule”로 작성된다\n" +
                "- \"title\" ⇒ 입력된 텍스트 본문이 입력되어야 한다\n" +
                "- \"startDateTime\" ⇒ 입력된 행위/행동의 시작 날짜/시간을 한국시간 기준으로 입력한다.\n" +
                "- \"endDateTime\" ⇒ 입력된 행위/행동의 종료 날짜/시간을 한국시간 기준으로 입력한다.\n" +
                "- \"startDateTime\" 과 \"endDateTime\"의 형태는 ISO 8601 Local Date-Time 형식으로 작성되어야 한다.";
    }

    // GPT 서버에 요청보내기 (aiRequest 생성)
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

    // GPT 서버에 요청을 보내고 응답 content를 반환하는 메서드 (aiRequest -> aiResponse)
//    public OpenAiResponse sendToGpt(OpenAiRequest request) throws IOException {
//        //GPT API에 보낼 POST 요청 생성
//        HttpPost post = new HttpPost(properties.getBaseUrl());
//        //요청 헤더 설정 : 인증토큰과 JSON 타입 명시
//        post.setHeader("Authorization", "Bearer " + properties.getApiKey());
//        post.setHeader("Content-Type", "application/json");
//        //ChatRequest 객체를 JSON 문자열로 직렬화 -> 요청 본문에 담기
//        post.setEntity(new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8));
//
//        //HTTP  클라이언트로 요청 전송 및 응답 수신
//        try (CloseableHttpClient client = HttpClients.createDefault();
//             CloseableHttpResponse response = client.execute(post)) {
//
//            //응답 JSON 문자열 꺼냄
//            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//            //JSON 문자열을 ChatResponse 객체로 역직렬화
//            return objectMapper.readValue(json, OpenAiResponse.class);
//        }
//    }

    // GPT 서버에 요청을 보내고 응답 content를 반환하는 메서드 (aiRequest -> aiResponse)
    //타임아웃 : 연결, 요청, 응답 각 60초로 제한 + 리트라이 : 최대 3번까지 재시도, 실패 시 예외 처리
    public OpenAiResponse sendToGpt(OpenAiRequest request) throws IOException {

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MILLIS)      // 연결 60초 제한
                .setConnectionRequestTimeout(TIMEOUT_MILLIS)     // 요청 정보 가져오는데 걸리는 시간 : 60초 제한
                .setSocketTimeout(TIMEOUT_MILLIS)       // 서버로부터 응답 기다리는 최대 시간 : 60초 제한
                .build();
        //HTTP  클라이언트로 요청 전송 및 응답 수신
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)    //모든 Http 요청에 대해 timeout 같은 설정을 자동으로 적용 해줌
                .build()) {                             //일관된 방식으로 안전하게 관리됨 : 무한정 버벅거림x, 재시도 or 실패 처리 가능

            int retryCount = 0;
                //리트라이 최대 3번까지 재시도, 실패 시 예외발생
            while (retryCount < MAX_RETRY_COUNT) {
                //GPT API에 보낼 POST 요청 생성
                HttpPost post = new HttpPost("https://api.openai.com/v1/chat/completions");
                //요청 헤더 설정 : 인증토큰과 JSON 타입 명시
                post.setHeader("Authorization", "Bearer " + properties.getApiKey());
                post.setHeader("Content-Type", "application/json");
                //ChatRequest 객체를 JSON 문자열로 직렬화 -> 요청 본문에 담기
                post.setEntity(new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8));


                try (CloseableHttpResponse response = client.execute(post)) {
                    //응답 JSON 문자열 꺼냄
                    String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    //JSON 문자열을 ChatResponse 객체로 역직렬화
                    return objectMapper.readValue(json, OpenAiResponse.class);
                } catch (IOException e) {
                    retryCount++;
//                    log.warn("GPT 요청 실패 - 재시도 {}회 (최대 {}회)", retryCount, MAX_RETRY_COUNT);
                    logStorageService.logAndStoreWithError("GPT 요청 실패 - 재시도 {}회 (최대 {}회)", logNameReport, retryCount, "3");
                    if (retryCount >= MAX_RETRY_COUNT) {
                        logStorageService.logAndStoreWithError("GPT 요청 최종 실패", logNameReport, e.getMessage(), e);
                        throw new BusinessLogicException(ExceptionCode.REPORT_GENERATION_FAILED);
                    }
                    try {
                        Thread.sleep(2000); // 재시도 전 2초 대기: 네트워크 일시장애도 커버 가능
                    } catch (InterruptedException interruptedException) {

                        Thread.currentThread().interrupt();
                        throw new BusinessLogicException(ExceptionCode.REPORT_GENERATION_FAILED);
                    }
                }
            }
        }
        logStorageService.logAndStoreWithError("GPT 요청 실패: {}", logNameReport, ExceptionCode.REPORT_GENERATION_FAILED.getMessage());
        //3번 실패하면 예외발생
        throw new BusinessLogicException(ExceptionCode.REPORT_GENERATION_FAILED); // 이론상 도달하지 않음
    }


    //aiResponse - content 파싱 (-> Map<K,V>)
    public String extractContent(OpenAiResponse response) {
        return response.getChoices().get(0).getMessage().getContent();
    }

}