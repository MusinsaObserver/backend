package observer.backend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import observer.backend.exception.ErrorCode;

@Getter
@AllArgsConstructor
public class ErrorResponse {
	private final ErrorCode errorCode;
	private final String message;
}