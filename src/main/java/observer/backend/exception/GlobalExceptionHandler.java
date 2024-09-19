package observer.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	// 모든 예외(Exception)를 처리하는 메소드
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> globalExceptionHandler(Exception ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 내부 서버 오류
	}

	// ResourceNotFoundException을 처리하는 메소드
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> resourceNotFoundExceptionHandler(ResourceNotFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // 404 리소스를 찾을 수 없음
	}

	// UnauthorizedException을 처리하는 메소드
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<?> unauthorizedExceptionHandler(UnauthorizedException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED); // 401 인증되지 않음
	}

	// ResourceNotFoundException 클래스 정의
	public static class ResourceNotFoundException extends RuntimeException {
		public ResourceNotFoundException(String message) {
			super(message);
		}
	}

	// UnauthorizedException 클래스 정의
	public static class UnauthorizedException extends RuntimeException {
		public UnauthorizedException(String message) {
			super(message);
		}
	}
}
