# FILE REPLACEMENT MAP

Both ZIPs are complete, copy-back-compatible trees: each mirrors the original archive layout, so you can unzip over the matching project root. Originals were not modified. `.env` (real secrets), `target/`, `node_modules/`, `dist/`, `.git/` and `.idea/` are excluded.

Legend: **REPLACE** = file exists in your project and is overwritten; **ADD** = new file; **DELETE** = remove this file from your project (copying the ZIP over your tree does not delete it, so delete it by hand).


## Backend (Maven root = `fidmap/`)

58 replaced, 55 added, 3 to delete.

### REPLACE

- `fidmap/pom.xml`
- `fidmap/src/main/java/com/amsal/fidmap/FidmapApplication.java`
- `fidmap/src/main/java/com/amsal/fidmap/authentication/AuthenticationController.java`
- `fidmap/src/main/java/com/amsal/fidmap/authentication/AuthenticationService.java`
- `fidmap/src/main/java/com/amsal/fidmap/board/BoardService.java`
- `fidmap/src/main/java/com/amsal/fidmap/changeLog/ChangeLogService.java`
- `fidmap/src/main/java/com/amsal/fidmap/comment/CommentMapper.java`
- `fidmap/src/main/java/com/amsal/fidmap/comment/CommentService.java`
- `fidmap/src/main/java/com/amsal/fidmap/config/FidmapConfig.java`
- `fidmap/src/main/java/com/amsal/fidmap/config/SecurityConfig.java`
- `fidmap/src/main/java/com/amsal/fidmap/endUser/EndUserRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/exception/GlobalExceptionHandler.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackController.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackMapper.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackPost.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackPostRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackService.java`
- `fidmap/src/main/java/com/amsal/fidmap/jwt/JwtAuthenticationFilter.java`
- `fidmap/src/main/java/com/amsal/fidmap/payment/billing/BillingService.java`
- `fidmap/src/main/java/com/amsal/fidmap/payment/billing/PlanEntitlementResponse.java`
- `fidmap/src/main/java/com/amsal/fidmap/payment/billing/PlanEntitlementService.java`
- `fidmap/src/main/java/com/amsal/fidmap/payment/polar/PolarWebhookService.java`
- `fidmap/src/main/java/com/amsal/fidmap/payment/subscription/Subscription.java`
- `fidmap/src/main/java/com/amsal/fidmap/roadmapItem/AddItemRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/roadmapItem/RoadmapItemDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/roadmapItem/RoadmapItemMapper.java`
- `fidmap/src/main/java/com/amsal/fidmap/roadmapItem/RoadmapItemService.java`
- `fidmap/src/main/java/com/amsal/fidmap/security/SecurityUtils.java`
- `fidmap/src/main/java/com/amsal/fidmap/security/WorkspaceAuthorizationService.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/CreateSurveyRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/Survey.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/SurveyDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/SurveyService.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/UpdateSurveyRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyAnswer/SurveyAnswerRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyOption/SurveyOptionDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyQuestion/CreateQuestionRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyQuestion/SurveyQuestion.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyQuestion/SurveyQuestionDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyQuestion/SurveyQuestionService.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyQuestion/UpdateQuestionRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/ISurveyResponseService.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/SubmitSurveyResponseRequest.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/SurveyResponse.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/SurveyResponseController.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/SurveyResponseDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/SurveyResponseRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/SurveyResponseService.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResult/SurveyResultsDto.java`
- `fidmap/src/main/java/com/amsal/fidmap/user/UserRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/user/UserService.java`
- `fidmap/src/main/java/com/amsal/fidmap/vote/Vote.java`
- `fidmap/src/main/java/com/amsal/fidmap/vote/VoteRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/vote/VoteService.java`
- `fidmap/src/main/java/com/amsal/fidmap/workspace/WorkspaceRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/workspace/WorkspaceService.java`
- `fidmap/src/main/resources/application.yaml`

### ADD

- `fidmap/src/main/java/com/amsal/fidmap/admin/AdminController.java`
- `fidmap/src/main/java/com/amsal/fidmap/audit/AuditLog.java`
- `fidmap/src/main/java/com/amsal/fidmap/audit/AuditLogRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/audit/AuditService.java`
- `fidmap/src/main/java/com/amsal/fidmap/common/PageResponse.java`
- `fidmap/src/main/java/com/amsal/fidmap/common/Paging.java`
- `fidmap/src/main/java/com/amsal/fidmap/config/RateLimitFilter.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/Confidence.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/DecisionController.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/DecisionDtos.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/DecisionService.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/EvidenceCollector.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/FeedbackPriorityInput.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/FeedbackPriorityInputRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/PriorityEvidence.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/PriorityResult.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/PriorityScoringEngine.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/ProductDecision.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/ProductDecisionRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/Recommendation.java`
- `fidmap/src/main/java/com/amsal/fidmap/decision/ScoreComponent.java`
- `fidmap/src/main/java/com/amsal/fidmap/duplicate/DuplicateController.java`
- `fidmap/src/main/java/com/amsal/fidmap/duplicate/DuplicateService.java`
- `fidmap/src/main/java/com/amsal/fidmap/duplicate/DuplicateSuggestion.java`
- `fidmap/src/main/java/com/amsal/fidmap/duplicate/DuplicateSuggestionRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/duplicate/SuggestionStatus.java`
- `fidmap/src/main/java/com/amsal/fidmap/duplicate/TextSimilarity.java`
- `fidmap/src/main/java/com/amsal/fidmap/endUser/EndUserResolver.java`
- `fidmap/src/main/java/com/amsal/fidmap/exception/SubscriptionRequiredException.java`
- `fidmap/src/main/java/com/amsal/fidmap/exception/SurveyNotFoundException.java`
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackDependentsCleaner.java`
- `fidmap/src/main/java/com/amsal/fidmap/insight/InsightsController.java`
- `fidmap/src/main/java/com/amsal/fidmap/insight/InsightsService.java`
- `fidmap/src/main/java/com/amsal/fidmap/invitation/InvitationController.java`
- `fidmap/src/main/java/com/amsal/fidmap/invitation/InvitationService.java`
- `fidmap/src/main/java/com/amsal/fidmap/invitation/InvitationStatus.java`
- `fidmap/src/main/java/com/amsal/fidmap/invitation/TeamInvitation.java`
- `fidmap/src/main/java/com/amsal/fidmap/invitation/TeamInvitationRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/mail/EmailDeliveryException.java`
- `fidmap/src/main/java/com/amsal/fidmap/mail/EmailService.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/FeedbackFollower.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/FeedbackFollowerRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/FeedbackNotification.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/FeedbackNotificationRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/NotificationController.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/NotificationService.java`
- `fidmap/src/main/java/com/amsal/fidmap/notification/NotificationStatus.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/SurveyMapper.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/survey/SurveyValidator.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyAnswer/SurveyAnswerRepository.java`
- `fidmap/src/main/java/com/amsal/fidmap/surveyFile/surveyResponse/PublicSurveyController.java`
- `fidmap/src/test/java/com/amsal/fidmap/decision/PriorityScoringEngineTest.java`
- `fidmap/src/test/java/com/amsal/fidmap/duplicate/TextSimilarityTest.java`
- `fidmap/src/test/java/com/amsal/fidmap/payment/billing/PlanEntitlementEvaluateTest.java`
- `fidmap/src/test/java/com/amsal/fidmap/surveyFile/survey/SurveyValidatorTest.java`

### DELETE (manual)

- `fidmap/src/main/java/com/amsal/fidmap/endUser/EndUserController.java` — superseded (see BACKEND_AUDIT.md)
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/FeedbackPriorityResponse.java` — superseded (see BACKEND_AUDIT.md)
- `fidmap/src/main/java/com/amsal/fidmap/feedbackPost/ImpactScoreService.java` — superseded (see BACKEND_AUDIT.md)

## Frontend (project root)

15 replaced, 19 added, 0 to delete.

### REPLACE

- `public/sitemap.xml`
- `src/App.jsx`
- `src/Header.jsx`
- `src/Style.css`
- `src/components/Api.js`
- `src/components/RoadmapItemModal.jsx`
- `src/constants/pricing.js`
- `src/layouts/MarketingLayout.jsx`
- `src/pages/Alternatives/Alternatives.jsx`
- `src/pages/Alternatives/CannyAlternative.jsx`
- `src/pages/Alternatives/FrillAlternative.jsx`
- `src/pages/Marketing.jsx`
- `src/pages/Pricing.jsx`
- `src/pages/Settings/SettingsTeam.jsx`
- `src/pages/StaffDashboard.jsx`

### ADD

- `src/components/PlanGuide.jsx`
- `src/hooks/useAsync.js`
- `src/pages/AcceptTeamInvite.jsx`
- `src/pages/Admin/PlatformWorkspaces.jsx`
- `src/pages/Alternatives/ComparisonPage.jsx`
- `src/pages/Alternatives/ProductboardAlternative.jsx`
- `src/pages/Alternatives/UserVoiceAlternative.jsx`
- `src/pages/Decisions/DecisionBrief.jsx`
- `src/pages/Decisions/Duplicates.jsx`
- `src/pages/Decisions/Priorities.jsx`
- `src/pages/Decisions/decisionUi.jsx`
- `src/pages/HowItWorks.jsx`
- `src/pages/Insights/Insights.jsx`
- `src/pages/Settings/TeamInvitations.jsx`
- `src/pages/Surveys/PublicSurvey.jsx`
- `src/pages/Surveys/SurveyBuilderModal.jsx`
- `src/pages/Surveys/SurveyResults.jsx`
- `src/pages/Surveys/Surveys.jsx`
- `src/pages/Unsubscribe.jsx`

### DELETE (manual)

- none

## Documentation added
See `docs/` in each ZIP: the 13 required `.md` files. Frontend also updates `SEO-CONTENT-MAP.md` and `BACKEND_FIXES.md`.


## Not in either ZIP
- Backend `.env` (contains real secrets; use `.env.example` / your host's secret store).
- Any generated build output.

## Also added (not in the lists above)
- Backend: `fidmap/.env.example` (placeholders only) and `fidmap/docs/*.md` (13 docs). ZIP path: `fidmap/fidmap/...`.
- Frontend: `docs/*.md` (13 docs; `docs/backend-api-requirements.md` pre-exists) and updated `BACKEND_FIXES.md`, `SEO-CONTENT-MAP.md`.
- ZIP layout: backend `fidmap/{Docker,fidmap/...}` (the `Docker` entry is copied unchanged; `target/`, `.idea/`, `.env`, `.git` omitted); frontend `fidmap_front_25/fidmap_front/...`. `.git` history is not included in either ZIP.
- Backend (added later): `fidmap/src/test/java/com/amsal/fidmap/config/RateLimitFilterTest.java`, the fifth new JUnit file (written, not executed).
