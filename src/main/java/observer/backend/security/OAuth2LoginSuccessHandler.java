package observer.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import observer.backend.entity.User;
import observer.backend.service.UserService;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserService userService;

	public OAuth2LoginSuccessHandler(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		// OAuth2User에서 Apple로부터 받은 사용자 정보 가져오기
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String appleUserId = (String) oAuth2User.getAttribute("sub");  // Apple에서 제공하는 고유 식별자

		// 애플이 이메일을 제공하는 경우만 저장 (최초 로그인 시에만 제공)
		String email = (String) oAuth2User.getAttribute("email");

		// userId를 사용하여 사용자 정보 조회 또는 생성
		User user = userService.findByAppleUserId(appleUserId).orElseGet(() -> {
			// 사용자 정보가 없을 경우 새 사용자 생성 로직
			User newUser = User.builder()
				.provider("apple")  // Apple을 제공자로 설정
				.providerId(appleUserId)  // Apple의 고유 식별자를 providerId로 사용
				.email(email)  // 이메일은 최초 로그인 시에만 저장됨
				.build();
			return userService.save(newUser);
		});

		// 세션에 사용자 정보 저장
		request.getSession().setAttribute("user", user);

		// 리디렉션 처리
		String redirectUrl = "http://localhost:8080/my_page_1";
		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}
}
