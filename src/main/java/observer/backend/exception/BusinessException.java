package observer.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
  private HttpStatus status;

  public BusinessException(HttpStatus status, ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.status = status;
  }

  public static class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
      super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_PRODUCT);
    }
  }
}
