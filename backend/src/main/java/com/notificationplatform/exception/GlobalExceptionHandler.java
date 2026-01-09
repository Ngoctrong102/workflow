package com.notificationplatform.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Resource not found [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WorkflowValidationException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowValidationException(WorkflowValidationException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Workflow validation error [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "WORKFLOW_INVALID_DEFINITION",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Validation error [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "VALIDATION_ERROR",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<ErrorResponse> handleExecutionException(ExecutionException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Execution error [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "WORKFLOW_EXECUTION_FAILED",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Conflict error [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "CONFLICT",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Business error [requestId={}, code={}]: {}", requestId, ex.getErrorCode(), ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                ex.getErrorCode(),
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Invalid argument [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Illegal state [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "ILLEGAL_STATE",
                ex.getMessage(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Validation error [requestId={}]: {}", requestId, ex.getMessage());
        
        List<Map<String, String>> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            Map<String, String> errorDetail = new HashMap<>();
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorDetail.put("field", fieldName);
            errorDetail.put("message", errorMessage);
            errors.add(errorDetail);
        });

        Map<String, Object> details = new HashMap<>();
        details.put("errors", errors);
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "VALIDATION_ERROR",
                "Validation failed",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Constraint violation [requestId={}]: {}", requestId, ex.getMessage());
        
        List<Map<String, String>> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            Map<String, String> errorDetail = new HashMap<>();
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errorDetail.put("field", propertyPath);
            errorDetail.put("message", message);
            errors.add(errorDetail);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("errors", errors);
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "VALIDATION_ERROR",
                "Validation failed",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Missing parameter [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getParameterName());
        details.put("type", ex.getParameterType());
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "MISSING_PARAMETER",
                "Required parameter is missing: " + ex.getParameterName(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Type mismatch [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getName());
        Class<?> requiredType = ex.getRequiredType();
        details.put("requiredType", requiredType != null ? requiredType.getName() : "unknown");
        details.put("value", ex.getValue());
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "INVALID_PARAMETER_TYPE",
                "Invalid parameter type for: " + ex.getName(),
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Invalid request body [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "INVALID_REQUEST_BODY",
                "Invalid request body format",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("File too large [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("max_size", ex.getMaxUploadSize());
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "FILE_TOO_LARGE",
                "File size exceeds maximum allowed size",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Data integrity violation [requestId={}]: {}", requestId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "DATA_INTEGRITY_VIOLATION",
                "Data integrity constraint violation",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Runtime error [requestId={}]: ", requestId, ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("request_id", requestId);
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Unexpected error [requestId={}]: ", requestId, ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("request_id", requestId);
        
        ErrorResponse.ErrorInfo errorInfo = new ErrorResponse.ErrorInfo(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                details,
                requestId,
                LocalDateTime.now()
        );
        ErrorResponse error = new ErrorResponse(errorInfo, requestId);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

