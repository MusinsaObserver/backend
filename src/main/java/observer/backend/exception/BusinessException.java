package observer.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
  private final HttpStatus status;
  private final ErrorCode errorCode;

  public BusinessException(HttpStatus status, ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.status = status;
    this.errorCode = errorCode;
  }

  // 사용자 관련 예외
  public static class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
      super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_USER_EXCEPTION);
    }
  }

  // 상품 관련 예외
  public static class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
      super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_PRODUCT);
    }
  }

  // 좋아요 관련 예외
  public static class ProductAlreadyLikedException extends BusinessException {
    public ProductAlreadyLikedException() {
      super(HttpStatus.CONFLICT, ErrorCode.ALREADY_LIKED_PRODUCT);
    }
  }

  public static class LikeNotFoundException extends BusinessException {
    public LikeNotFoundException() {
      super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_LIKE);
    }
  }
}