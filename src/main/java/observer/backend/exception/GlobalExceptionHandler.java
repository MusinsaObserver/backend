package observer.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import observer.backend.response.ErrorResponse;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		log.error("Business Exception: {}", ex.getMessage());
		ErrorResponse response = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
		return new ResponseEntity<>(response, ex.getStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
		log.error("Unexpected Error: ", ex);
		ErrorResponse response = new ErrorResponse(
			ErrorCode.INTERNAL_SERVER_ERROR,
			"서버 내부 오류가 발생했습니다."
		);
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}