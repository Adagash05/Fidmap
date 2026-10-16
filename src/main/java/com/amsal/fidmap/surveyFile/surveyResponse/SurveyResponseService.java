package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.common.Paging;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserResolver;
import com.amsal.fidmap.exception.SurveyNotFoundException;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.surveyFile.survey.SurveyDto;
import com.amsal.fidmap.surveyFile.survey.SurveyMapper;
import com.amsal.fidmap.surveyFile.survey.SurveyRepository;
import com.amsal.fidmap.surveyFile.survey.SurveyStatus;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswer;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerDto;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerRepository;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerRequest;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import com.amsal.fidmap.surveyFile.surveyQuestion.QuestionType;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.surveyFile.surveyResult.SurveyResultsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyResponseService implements ISurveyResponseService {

    static final int MAX_TEXT_LENGTH = 2000;
    private static final int TEXT_PREVIEW_LIMIT = 50;

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;
    private final EndUserResolver endUserResolver;
    private final PlanEntitlementService planEntitlementService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;

    @Override
    public ApiResponse<SurveyResponseDto> submitResponse(Long surveyId, SubmitSurveyResponseRequest request) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey not found"));

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new IllegalStateException("This survey is not accepting responses");
        }

        if (request == null || request.respondent() == null) {
            throw new IllegalArgumentException("Your name and email are required");
        }

        planEntitlementService.requireActiveAccess(survey.getWorkspace().getId());

        List<SurveyAnswerRequest> submitted = request.answers() == null ? List.of() : request.answers();

        List<SurveyAnswer> answers = validateAnswers(survey, submitted);

        EndUser respondent = endUserResolver.resolve(
                survey.getWorkspace(),
                request.respondent().getName(),
                request.respondent().getEmail()
        );

        // Duplicate-submission policy: one response per respondent per survey.
        if (responseRepository.existsBySurveyIdAndEndUserId(surveyId, respondent.getId())) {
            throw new IllegalStateException("You have already responded to this survey");
        }

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .endUser(respondent)
                .submittedAt(LocalDateTime.now())
                .build();

        answers.forEach(response::addAnswer);

        SurveyResponse saved;

        try {
            saved = responseRepository.saveAndFlush(response);
        } catch (DataIntegrityViolationException e) {
            // Concurrent duplicate caught by the unique (survey, respondent) constraint.
            throw new IllegalStateException("You have already responded to this survey");
        }

        return ApiResponse.success("Survey response submitted successfully", mapToDto(saved));
    }

    /**
     * Validates the submitted answers against the survey definition and
     * builds the answer rows. Pure with respect to the database.
     */
    List<SurveyAnswer> validateAnswers(Survey survey, List<SurveyAnswerRequest> submitted) {

        Map<Long, SurveyQuestion> byId = survey.getQuestions().stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        Map<Long, SurveyAnswerRequest> answered = new HashMap<>();

        for (SurveyAnswerRequest a : submitted) {

            if (a == null || a.questionId() == null || !byId.containsKey(a.questionId())) {
                throw new IllegalArgumentException("Answer refers to a question that is not part of this survey");
            }

            if (answered.put(a.questionId(), a) != null) {
                throw new IllegalArgumentException("A question can only be answered once");
            }
        }

        List<SurveyAnswer> rows = new ArrayList<>();

        for (SurveyQuestion question : survey.getQuestions()) {

            SurveyAnswerRequest a = answered.get(question.getId());

            boolean provided = a != null && (
                    (a.answer() != null && !a.answer().isBlank())
                            || (a.answers() != null && !a.answers().isEmpty()));

            if (!provided) {

                if (Boolean.TRUE.equals(question.getRequired())) {
                    throw new IllegalArgumentException("Please answer: " + question.getQuestion());
                }

                continue;
            }

            switch (question.getType()) {

                case TEXT -> {
                    String text = a.answer().trim();

                    if (text.length() > MAX_TEXT_LENGTH) {
                        throw new IllegalArgumentException("Text answers can be at most " + MAX_TEXT_LENGTH + " characters");
                    }

                    rows.add(row(question, text));
                }

                case RATING -> {
                    int rating;

                    try {
                        rating = Integer.parseInt(a.answer().trim());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Ratings must be a whole number from 1 to 5");
                    }

                    if (rating < 1 || rating > 5) {
                        throw new IllegalArgumentException("Ratings must be a whole number from 1 to 5");
                    }

                    rows.add(row(question, String.valueOf(rating)));
                }

                case SINGLE_CHOICE -> {
                    String value = a.answer().trim();

                    if (!optionValues(question).contains(value)) {
                        throw new IllegalArgumentException("Choose one of the listed options");
                    }

                    rows.add(row(question, value));
                }

                case MULTIPLE_CHOICE -> {
                    List<String> values = a.answers() != null && !a.answers().isEmpty()
                            ? a.answers()
                            : List.of(a.answer());

                    Set<String> allowed = optionValues(question);
                    Set<String> unique = new HashSet<>();

                    for (String v : values) {

                        String value = v == null ? "" : v.trim();

                        if (!allowed.contains(value)) {
                            throw new IllegalArgumentException("Choose only from the listed options");
                        }

                        if (unique.add(value)) {
                            rows.add(row(question, value));
                        }
                    }
                }
            }
        }

        return rows;
    }

    private static Set<String> optionValues(SurveyQuestion question) {
        return question.getOptions().stream().map(SurveyOption::getValue).collect(Collectors.toSet());
    }

    private static SurveyAnswer row(SurveyQuestion question, String value) {
        return SurveyAnswer.builder().question(question).answer(value).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SurveyDto> getPublicSurvey(Long surveyId) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey not found"));

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new SurveyNotFoundException("Survey not found");
        }

        return ApiResponse.success("Survey retrieved successfully", SurveyMapper.toDto(survey, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<SurveyResponseDto>> getSurveyResponses(Long surveyId, Integer page, Integer size) {

        Survey survey = staffSurvey(surveyId);

        var result = responseRepository.findBySurveyId(
                survey.getId(),
                Paging.of(page, size, Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id")))
        );

        return ApiResponse.success(
                "Survey responses retrieved successfully",
                PageResponse.of(result, this::mapToDto)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SurveyResultsDto> getResults(Long surveyId) {

        Survey survey = staffSurvey(surveyId);

        // questionId -> (answer -> count)
        Map<Long, Map<String, Long>> counts = new HashMap<>();

        for (Object[] row : answerRepository.countAnswers(surveyId)) {
            counts.computeIfAbsent((Long) row[0], k -> new LinkedHashMap<>())
                    .put((String) row[1], (Long) row[2]);
        }

        List<SurveyResultsDto.QuestionResult> results = new ArrayList<>();

        for (SurveyQuestion question : survey.getQuestions()) {

            Map<String, Long> distribution = counts.getOrDefault(question.getId(), Map.of());

            long answerCount = distribution.values().stream().mapToLong(Long::longValue).sum();

            Double average = null;
            List<String> texts = List.of();

            if (question.getType() == QuestionType.RATING && answerCount > 0) {

                double sum = 0;

                for (var entry : distribution.entrySet()) {
                    sum += Double.parseDouble(entry.getKey()) * entry.getValue();
                }

                average = sum / answerCount;
            }

            if (question.getType() == QuestionType.TEXT) {

                texts = answerRepository
                        .latestTextAnswers(question.getId(), PageRequest.of(0, TEXT_PREVIEW_LIMIT))
                        .stream()
                        .map(SurveyAnswer::getAnswer)
                        .toList();

                answerCount = texts.size();
            }

            results.add(new SurveyResultsDto.QuestionResult(
                    question.getId(),
                    question.getQuestion(),
                    question.getType(),
                    answerCount,
                    distribution,
                    average,
                    texts
            ));
        }

        return ApiResponse.success(
                "Survey results retrieved successfully",
                new SurveyResultsDto(
                        survey.getId(),
                        survey.getTitle(),
                        responseRepository.countBySurveyId(surveyId),
                        results
                )
        );
    }

    private Survey staffSurvey(Long surveyId) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey not found"));

        workspaceAuthorizationService.requireStaff(survey.getWorkspace().getId());

        return survey;
    }

    private SurveyResponseDto mapToDto(SurveyResponse response) {

        List<SurveyAnswerDto> answers = response.getAnswers()
                .stream()
                .map(answer -> new SurveyAnswerDto(
                        answer.getId(),
                        answer.getQuestion().getId(),
                        answer.getAnswer()
                ))
                .toList();

        EndUser respondent = response.getEndUser();

        return new SurveyResponseDto(
                response.getId(),
                response.getSurvey().getId(),
                respondent == null ? null : respondent.getName(),
                respondent == null ? null : respondent.getEmail(),
                response.getSubmittedAt(),
                answers
        );
    }
}
