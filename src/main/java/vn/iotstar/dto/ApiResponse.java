package vn.iotstar.dto;

public class ApiResponse<T> {
    private final boolean status;
    private final String message;
    private final T body;

    public ApiResponse(boolean status, String message, T body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }

    public static <T> ApiResponse<T> success(String message, T body) {
        return new ApiResponse<>(true, message, body);
    }

    public static <T> ApiResponse<T> failure(String message, T body) {
        return new ApiResponse<>(false, message, body);
    }

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public T getBody() { return body; }
}
