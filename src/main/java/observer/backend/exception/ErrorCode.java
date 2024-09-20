package observer.backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // user
  FAILED_ADMIN_PASSWORD_EXCEPTION("관리자 암호 인증 실패해서 가입이 불가능합니다."),
  ALREADY_EXIST_USER_EMAIL_EXCEPTION("이미 존재하는 이메일 입니다."),
  ALREADY_EXIST_USER_NICKNAME_EXCEPTION("이미 존재하는 닉네임 입니다."),
  NOT_EQUALS_CONFIRM_PASSWORD_EXCEPTION("비밀번호 확인이 일치하지 않습니다."),
  FAILED_AUTHENTICATION_EXCEPTION("인증에 실패하였습니다."),
  NOT_FOUND_USER_EXCEPTION("해당 유저는 없습니다."),
  FAILED_EMAIL_SEND_EXCEPTION("이메일 서버 문제 or 잘못된 이메일 주소 입니다"),
  FAILED_EMAIL_AUTHENTICATION_EXCEPTION("이메일 인증번호 일치하지 않습니다."),
  EMAIL_VERIFICATION_NEEDED("이메일 인증이 필요 합니다"),
  AUTHENTICATION_EXCEPTION("로그인하고 이용해주세요."),
  INVALID_TOKEN_EXCEPTION("유효하지 않은 토큰 입니다."),
  AUTHENTICATION_MISMATCH_EXCEPTION("권한이 없습니다."),
  LOGIN_REQUIRED_EXCEPTION("리프레시 토큰 문제 있으니 다시 로그인 해주세요."),

  // product
  NOT_FOUND_PRODUCT("해당 상품을 찾을 수 없습니다.");



  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }

}