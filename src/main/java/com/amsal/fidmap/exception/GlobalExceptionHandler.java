package com.amsal.fidmap.exception;

import com.amsal.fidmap.apiResponse.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBoardNotFoundException(BoardNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());

        //Return to postman
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(ChangeLogNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleChangeLogNotFoundException(ChangeLogNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(EndUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleEndUserException(EndUserException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(FeedbackPostNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeedbackPostNotFoundException(FeedbackPostNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(RoadmapNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoadmapNotFoundException(RoadmapNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(WorkspaceMembershipNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleWorkspaceMembershipNotFoundException(WorkspaceMembershipNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleWorkspaceNotFoundException(WorkspaceNotFoundException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, HttpServletRequest request) {

        //print log to intellij (logs)
        System.err.println(ex.getMessage());


        //Return to postman

         return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        System.err.println(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlanLimitExceededException(
            PlanLimitExceededException ex,
            HttpServletRequest request
    ) {

        System.err.println(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(FeatureNotAvailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeatureNotAvailableException(
            FeatureNotAvailableException ex,
            HttpServletRequest request
    ) {

        System.err.println(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

//    @ExceptionHandler(IllegalStateException.class)
//    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
//            IllegalStateException ex,
//            HttpServletRequest request
//    ) {
//
//        System.err.println(ex.getMessage());
//
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(ApiResponse.error(ex.getMessage()));
//    }
}
