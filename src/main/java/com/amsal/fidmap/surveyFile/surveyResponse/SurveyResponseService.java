package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.security.SecurityUtils;
import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.surveyFile.survey.SurveyRepository;
import com.amsal.fidmap.surveyFile.survey.SurveyStatus;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswer;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerDto;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerRequest;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestionRepository;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyResponseService implements ISurveyResponseService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyResponseRepository responseRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<SurveyResponseDto> submitResponse(Long surveyId, SubmitSurveyResponseRequest request) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new IllegalStateException("This survey is not accepting responses");
        }

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(currentUserId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .user(user)
                .build();

        for (SurveyAnswerRequest answerRequest : request.answers()) {

            SurveyQuestion question = questionRepository.findById(answerRequest.questionId())
                            .orElseThrow(() -> new RuntimeException("Question not found"));

            if (!question.getSurvey().getId().equals(surveyId)) {

                throw new IllegalArgumentException("Question does not belong to this survey");
            }

            SurveyAnswer answer = SurveyAnswer.builder()
                    .question(question)
                    .answer(answerRequest.answer())
                    .build();

            response.addAnswer(answer);
        }

        SurveyResponse saved = responseRepository.save(response);

        return ApiResponse.success("Survey response submitted successfully", mapToDto(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SurveyResponseDto>> getSurveyResponses(Long surveyId) {

        if (!surveyRepository.existsById(surveyId)) {
            throw new RuntimeException("Survey not found");
        }

        List<SurveyResponseDto> responses = responseRepository.findAllBySurveyId(surveyId)
                        .stream()
                        .map(this::mapToDto)
                        .toList();

        return ApiResponse.success("Survey responses retrieved successfully", responses);
    }

    private SurveyResponseDto mapToDto(SurveyResponse response) {

        UUID userId = response.getUser() == null ? null : response.getUser().getId();

        List<SurveyAnswerDto> answers =
                response.getAnswers()
                        .stream()
                        .map(answer -> new SurveyAnswerDto(
                                        answer.getId(),
                                        answer.getQuestion().getId(),
                                        answer.getAnswer()
                                )
                        )
                        .toList();

        return new SurveyResponseDto(
                response.getId(),
                response.getSurvey().getId(),
                userId,
                response.getSubmittedAt(),
                answers
        );
    }
}