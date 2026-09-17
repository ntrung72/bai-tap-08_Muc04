package vn.iotstar.controller.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.iotstar.dto.ApiResponse;
import vn.iotstar.exception.ImageStorageException;

@RestControllerAdvice(basePackages = "vn.iotstar.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("Dữ liệu gửi lên không hợp lệ.", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ex.getMessage(), null));
    }

    @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        String message = ex instanceof DataIntegrityViolationException
                ? "Không thể thực hiện vì dữ liệu đang được sử dụng."
                : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody() {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("Nội dung JSON không đúng định dạng.", null));
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageStorage(ImageStorageException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ex.getMessage(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize() {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("Ảnh không được vượt quá 5 MB.", null));
    }
}
