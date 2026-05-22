package sv.edu.udb.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sv.edu.udb.dto.response.ApiErrorResponse;
import sv.edu.udb.exception.BusinessRuleException;
import sv.edu.udb.exception.ConflictException;
import sv.edu.udb.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AdviceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdviceController.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidations(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(buildError(400, "Validación inválida", request, errors));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(404).body(buildError(404, ex.getMessage(), request, null));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(buildError(409, ex.getMessage(), request, null));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return ResponseEntity.unprocessableEntity().body(buildError(422, ex.getMessage(), request, null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(401).body(buildError(401, ex.getMessage(), request, null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(403).body(buildError(403, ex.getMessage(), request, null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        LOGGER.error("Error de ejecución en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.internalServerError().body(buildError(500, "Error interno del servidor", request, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOGGER.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.internalServerError().body(buildError(500, "Error interno del servidor", request, null));
    }

    private ApiErrorResponse buildError(
            int status,
            String message,
            HttpServletRequest request,
            Map<String, String> validations
    ) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(resolveErrorLabel(status))
                .message(message)
                .path(request.getRequestURI())
                .validations(validations)
                .build();
    }

    private String resolveErrorLabel(int status) {
        return switch (status) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 422 -> "UNPROCESSABLE_ENTITY";
            default -> "INTERNAL_SERVER_ERROR";
        };
    }
}
