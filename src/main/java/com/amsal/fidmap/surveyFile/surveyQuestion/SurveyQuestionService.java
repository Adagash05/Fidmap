package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.SurveyNotFoundException;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.surveyFile.survey.SurveyMapper;
import com.amsal.fidmap.surveyFile.survey.SurveyRepository;
import com.amsal.fidmap.surveyFile.survey.SurveyStatus;
import com.amsal.fidmap.surveyFile.survey.SurveyValidator;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyQuestionService implements ISurveyQuestionService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;

    @Override
    public ApiResponse<SurveyQuestionDto> addQuestion(Long surveyId, CreateQuestionRequest request) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey not found"));

        workspaceAuthorizationService.requireStaffWrite(survey.getWorkspace().getId());
        requireDraft(survey, "add questions to");

        SurveyValidator.validateQuestion(request.question(), request.type(), request.options());

        if (survey.getQuestions().size() >= SurveyValidator.MAX_QUESTIONS) {
            throw new IllegalArgumentException(
                    "A survey can have at most " + SurveyValidator.MAX_QUESTIONS + " questions");
        }

        SurveyQuestion question = SurveyQuestion.builder()
                .question(request.question().trim())
                .type(request.type())
                .position(request.position() == null ? survey.getQuestions().size() : request.position())
                .required(Boolean.TRUE.equals(request.required()))
                .survey(survey)
                .build();

        replaceOptions(question, request.options());
        survey.getQuestions().add(question);

        SurveyQuestion saved = questionRepository.save(question);

        return ApiResponse.success("Question added successfully", SurveyMapper.toDto(saved));
    }

    @Override
    public ApiResponse<SurveyQuestionDto> updateQuestion(Long questionId, UpdateQuestionRequest request) {

        SurveyQuestion question = load(questionId);

        workspaceAuthorizationService.requireStaffWrite(question.getSurvey().getWorkspace().getId());
        requireDraft(question.getSurvey(), "update questions in");

        String text = request.question() != null ? request.question() : question.getQuestion();
        QuestionType type = request.type() != null ? request.type() : question.getType();

        List<String> options = request.options() != null
                ? request.options()
                : question.getOptions().stream().map(SurveyOption::getValue).toList();

        // Switching to a non-choice type drops stale options.
        if (type != QuestionType.SINGLE_CHOICE && type != QuestionType.MULTIPLE_CHOICE
                && request.options() == null) {
            options = List.of();
        }

        SurveyValidator.validateQuestion(text, type, options);

        question.setQuestion(text.trim());
        question.setType(type);

        if (request.position() != null) {
            question.setPosition(request.position());
        }

        if (request.required() != null) {
            question.setRequired(request.required());
        }

        if (request.options() != null || options.isEmpty()) {
            replaceOptions(question, options);
        }

        return ApiResponse.success("Question updated successfully", SurveyMapper.toDto(question));
    }

    @Override
    public ApiResponse<Void> deleteQuestion(Long questionId) {

        SurveyQuestion question = load(questionId);

        workspaceAuthorizationService.requireStaffWrite(question.getSurvey().getWorkspace().getId());
        requireDraft(question.getSurvey(), "delete questions from");

        question.getSurvey().getQuestions().remove(question);
        questionRepository.delete(question);

        return ApiResponse.success("Question deleted successfully", null);
    }

    private SurveyQuestion load(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new SurveyNotFoundException("Question not found"));
    }

    private void requireDraft(Survey survey, String verb) {
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Cannot " + verb + " a survey that is not a draft");
        }
    }

    private void replaceOptions(SurveyQuestion question, List<String> values) {

        question.getOptions().clear();

        if (values == null) {
            return;
        }

        int position = 0;

        for (String value : values) {
            question.addOption(SurveyOption.builder()
                    .value(value.trim())
                    .position(position++)
                    .build());
        }
    }
}
