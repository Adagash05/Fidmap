package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.SurveyNotFoundException;
import com.amsal.fidmap.exception.WorkspaceNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import com.amsal.fidmap.surveyFile.surveyQuestion.CreateQuestionRequest;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.surveyFile.surveyResponse.SurveyResponseRepository;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyService implements ISurveyService {

    private final SurveyRepository surveyRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SurveyResponseRepository responseRepository;
    private final FeedbackPostRepository feedbackPostRepository;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;

    @PersistenceContext
    private EntityManager em;

    @Override
    public ApiResponse<SurveyDto> createSurvey(UUID workspaceId, CreateSurveyRequest request) {

        workspaceAuthorizationService.requireStaffWrite(workspaceId);

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);

        if (workspace == null) {
            throw new WorkspaceNotFoundException("workspace not found");
        }

        int questionCount = request.questions() == null ? 0 : request.questions().size();

        SurveyValidator.validateSurvey(request.title(), questionCount);

        Survey survey = Survey.builder()
                .title(request.title().trim())
                .description(request.description())
                .status(SurveyStatus.DRAFT)
                .workspace(workspace)
                .linkedFeedbackPost(resolveLinkedPost(request.linkedFeedbackPostId(), workspaceId))
                .build();

        if (request.questions() != null) {

            int index = 0;

            for (CreateQuestionRequest questionRequest : request.questions()) {

                SurveyValidator.validateQuestion(
                        questionRequest.question(), questionRequest.type(), questionRequest.options());

                SurveyQuestion question = SurveyQuestion.builder()
                        .question(questionRequest.question().trim())
                        .type(questionRequest.type())
                        .position(questionRequest.position() == null ? index : questionRequest.position())
                        .required(Boolean.TRUE.equals(questionRequest.required()))
                        .build();

                index++;

                survey.addQuestion(question);

                if (questionRequest.options() != null) {

                    int position = 0;

                    for (String value : questionRequest.options()) {

                        question.addOption(
                                SurveyOption.builder().value(value.trim()).position(position++).build()
                        );
                    }
                }
            }
        }

        Survey saved = surveyRepository.save(survey);

        return ApiResponse.success("Survey created successfully", SurveyMapper.toDto(saved, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SurveyDto> getSurvey(Long surveyId) {

        Survey survey = loadForStaff(surveyId, false);

        return ApiResponse.success(
                "Survey retrieved successfully",
                SurveyMapper.toDto(survey, responseRepository.countBySurveyId(surveyId))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SurveyDto>> getWorkspaceSurveys(UUID workspaceId) {

        workspaceAuthorizationService.requireStaff(workspaceId);

        List<SurveyDto> surveys = surveyRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(s -> SurveyMapper.toDto(s, responseRepository.countBySurveyId(s.getId())))
                .toList();

        return ApiResponse.success("Surveys retrieved successfully", surveys);
    }

    @Override
    public ApiResponse<SurveyDto> updateSurvey(Long surveyId, UpdateSurveyRequest request) {

        Survey survey = loadForStaff(surveyId, true);

        if (survey.getStatus() == SurveyStatus.CLOSED) {
            throw new IllegalStateException("A closed survey cannot be updated");
        }

        SurveyValidator.validateSurvey(request.title(), survey.getQuestions().size());

        survey.setTitle(request.title().trim());
        survey.setDescription(request.description());
        survey.setLinkedFeedbackPost(
                resolveLinkedPost(request.linkedFeedbackPostId(), survey.getWorkspace().getId())
        );

        surveyRepository.save(survey);

        return ApiResponse.success(
                "Survey updated successfully",
                SurveyMapper.toDto(survey, responseRepository.countBySurveyId(surveyId))
        );
    }

    @Override
    public ApiResponse<SurveyDto> publishSurvey(Long surveyId) {

        Survey survey = loadForStaff(surveyId, true);

        if (survey.getStatus() == SurveyStatus.CLOSED) {
            throw new IllegalStateException("A closed survey cannot be published again");
        }

        if (survey.getQuestions().isEmpty()) {
            throw new IllegalStateException("A survey must contain at least one question");
        }

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            survey.setStatus(SurveyStatus.PUBLISHED);
            survey.setPublishedAt(LocalDateTime.now());
        }

        surveyRepository.save(survey);

        return ApiResponse.success(
                "Survey published successfully",
                SurveyMapper.toDto(survey, responseRepository.countBySurveyId(surveyId))
        );
    }

    @Override
    public ApiResponse<SurveyDto> closeSurvey(Long surveyId) {

        // Closing stays allowed for lapsed workspaces (it only stops collection).
        Survey survey = loadForStaff(surveyId, false);

        if (survey.getStatus() != SurveyStatus.CLOSED) {
            survey.setStatus(SurveyStatus.CLOSED);
            survey.setClosedAt(LocalDateTime.now());
        }

        surveyRepository.save(survey);

        return ApiResponse.success(
                "Survey closed successfully",
                SurveyMapper.toDto(survey, responseRepository.countBySurveyId(surveyId))
        );
    }

    @Override
    public ApiResponse<Void> deleteSurvey(Long surveyId) {

        Survey survey = loadForStaff(surveyId, false);

        em.createQuery("""
                DELETE FROM SurveyAnswer a
                WHERE a.response.id IN (SELECT r.id FROM SurveyResponse r WHERE r.survey.id = :id)
                """).setParameter("id", surveyId).executeUpdate();

        em.createQuery("DELETE FROM SurveyResponse r WHERE r.survey.id = :id")
                .setParameter("id", surveyId).executeUpdate();

        em.flush();
        em.clear();

        surveyRepository.deleteById(survey.getId());

        return ApiResponse.success("Survey deleted successfully", null);
    }

    /**
     * Loads a survey and enforces that the caller is staff of the survey's
     * workspace (and, for edits, that the plan is active).
     */
    Survey loadForStaff(Long surveyId, boolean write) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey not found"));

        UUID workspaceId = survey.getWorkspace().getId();

        if (write) {
            workspaceAuthorizationService.requireStaffWrite(workspaceId);
        } else {
            workspaceAuthorizationService.requireStaff(workspaceId);
        }

        return survey;
    }

    private FeedbackPost resolveLinkedPost(UUID postId, UUID workspaceId) {

        if (postId == null) {
            return null;
        }

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(postId);

        if (post == null
                || post.getWorkspace() == null
                || !workspaceId.equals(post.getWorkspace().getId())) {
            throw new AccessDeniedException("You do not have access to the selected request");
        }

        return post;
    }
}
