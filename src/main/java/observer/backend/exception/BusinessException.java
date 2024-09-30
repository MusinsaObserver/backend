package observer.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
  private HttpStatus status;

  public BusinessException(HttpStatus status,ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.status = status;
  }

  public BusinessException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
  }

  public String getErrorCode() {
    return getMessage();
  }
}