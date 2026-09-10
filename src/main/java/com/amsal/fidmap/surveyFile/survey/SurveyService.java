package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.UserNotFoundException;
import com.amsal.fidmap.security.SecurityUtils;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOptionDto;
import com.amsal.fidmap.surveyFile.surveyQuestion.CreateQuestionRequest;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestionDto;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyService implements ISurveyService {

    private final SurveyRepository surveyRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<SurveyDto> createSurvey(UUID workspaceId, CreateSurveyRequest request) {


        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(currentUserId);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }


//        WorkspaceMembership membership = workspaceMembershipService.getMembership(currentUserId, workspaceId);

//        Workspace workspace = membership.getWorkspace();

        Survey survey = Survey.builder()
                .title(request.title())
                .description(request.description())
                .status(SurveyStatus.DRAFT)
                .workspace(user.getWorkspace())
                .build();

        if (request.questions() != null) {

            for (CreateQuestionRequest questionRequest : request.questions()) {

                SurveyQuestion question = SurveyQuestion.builder()
                        .question(questionRequest.question())
                        .type(questionRequest.type())
                        .position(questionRequest.position())
                        .build();

                survey.addQuestion(question);

                if (questionRequest.options() != null) {

                    int position = 0;

                    for (String value : questionRequest.options()) {

                        SurveyOption option = SurveyOption.builder()
                                .value(value)
                                .position(position++)
                                .build();

                        question.addOption(option);
                    }
                }
            }
        }

        Survey savedSurvey = surveyRepository.save(survey);

        return ApiResponse.success("Survey created successfully", mapToDto(savedSurvey));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SurveyDto> getSurvey(Long surveyId) {

        Survey survey = getSurveyEntity(surveyId);

        return ApiResponse.success("Survey retrieved successfully", mapToDto(survey));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SurveyDto>> getWorkspaceSurveys(UUID workspaceId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(currentUserId);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }


        List<SurveyDto> surveys = surveyRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(this::mapToDto)
                .toList();

        return ApiResponse.success("Surveys retrieved successfully", surveys);
    }

    @Override
    public ApiResponse<SurveyDto> updateSurvey(Long surveyId, UpdateSurveyRequest request) {

        Survey survey = getSurveyEntity(surveyId);

        //todo for the exception
        if (survey.getStatus() == SurveyStatus.CLOSED) {
            throw new IllegalStateException("A closed survey cannot be updated");
        }

        survey.setTitle(request.title());
        survey.setDescription(request.description());

        surveyRepository.save(survey);

        return ApiResponse.success("Survey updated successfully", mapToDto(survey));
    }

    @Override
    public ApiResponse<SurveyDto> publishSurvey(Long surveyId) {

        Survey survey = getSurveyEntity(surveyId);

        if (survey.getQuestions().isEmpty()) {
            throw new IllegalStateException("A survey must contain at least one question");
        }

        survey.setStatus(SurveyStatus.PUBLISHED);
        survey.setPublishedAt(LocalDateTime.now());

        surveyRepository.save(survey);

        return ApiResponse.success("Survey published successfully", mapToDto(survey));
    }

    @Override
    public ApiResponse<SurveyDto> closeSurvey(Long surveyId) {

        Survey survey = getSurveyEntity(surveyId);

        survey.setStatus(SurveyStatus.CLOSED);
        surveyRepository.save(survey);


        return ApiResponse.success("Survey closed successfully", mapToDto(survey));
    }

    @Override
    public ApiResponse<Void> deleteSurvey(Long surveyId) {

        Survey survey = getSurveyEntity(surveyId);

        surveyRepository.delete(survey);

        return ApiResponse.success("Survey deleted successfully", null);
    }

    private Survey getSurveyEntity(Long surveyId) {

        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
    }

    private SurveyDto mapToDto(Survey survey) {

        List<SurveyQuestionDto> questions = survey.getQuestions()
                        .stream()
                        .sorted(comparing(SurveyQuestion::getPosition))
                        .map(question ->
                                new SurveyQuestionDto(
                                        question.getId(),
                                        question.getQuestion(),
                                        question.getType(),
                                        question.getPosition(),
                                        question.getOptions()
                                                .stream()
                                                .sorted(comparing(SurveyOption::getPosition))
                                                .map(option -> new SurveyOptionDto(
                                                                option.getId(),
                                                                option.getValue(),
                                                                option.getPosition()
                                                        )
                                                )
                                                .toList()
                                )
                        )
                        .toList();

        return new SurveyDto(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStatus(),
                survey.getPublishedAt(),
                questions
        );
    }
}
