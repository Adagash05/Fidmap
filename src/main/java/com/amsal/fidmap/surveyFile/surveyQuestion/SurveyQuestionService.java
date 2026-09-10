package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.surveyFile.survey.SurveyRepository;
import com.amsal.fidmap.surveyFile.survey.SurveyStatus;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyQuestionService
        implements ISurveyQuestionService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;

    @Override
    public ApiResponse<SurveyQuestionDto> addQuestion(Long surveyId, CreateQuestionRequest request) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (survey.getStatus() == SurveyStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Cannot add questions to a published survey"
            );
        }

        SurveyQuestion question = SurveyQuestion.builder()
                .question(request.question())
                .type(request.type())
                .position(request.position())
                .survey(survey)
                .build();

        if (request.options() != null) {

            int position = 0;

            for (String value : request.options()) {

                SurveyOption option = SurveyOption.builder()
                        .value(value)
                        .position(position++)
                        .question(question)
                        .build();

                question.getOptions().add(option);
            }
        }

        SurveyQuestion saved =
                questionRepository.save(question);

        return ApiResponse.success("Question added successfully", mapToDto(saved));
    }

    @Override
    public ApiResponse<SurveyQuestionDto> updateQuestion(Long questionId, UpdateQuestionRequest request) {

        SurveyQuestion question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        if (question.getSurvey().getStatus() == SurveyStatus.PUBLISHED) {

            throw new IllegalStateException("Cannot update a question in a published survey");
        }

        question.setQuestion(request.question());
        question.setType(request.type());
        question.setPosition(request.position());

        return ApiResponse.success("Question updated successfully", mapToDto(question));
    }

    @Override
    public ApiResponse<Void> deleteQuestion(Long questionId) {

        SurveyQuestion question = questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException("Question not found"));

        if (question.getSurvey().getStatus() == SurveyStatus.PUBLISHED) {

            throw new IllegalStateException("Cannot delete a question from a published survey");
        }

        questionRepository.delete(question);

        return ApiResponse.success("Question deleted successfully", null);
    }

    private SurveyQuestionDto mapToDto(SurveyQuestion question) {

        return new SurveyQuestionDto(
                question.getId(),
                question.getQuestion(),
                question.getType(),
                question.getPosition(),
                question.getOptions()
                        .stream()
                        .map(option ->
                                new SurveyOptionDto(
                                        option.getId(),
                                        option.getValue(),
                                        option.getPosition()
                                )
                        )
                        .toList()
        );
    }
}