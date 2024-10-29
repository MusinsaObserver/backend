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
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String appleUserId = (String) oAuth2User.getAttribute("sub");

		// 새로운 사용자 생성 또는 기존 사용자 가져오기
		User user = userService.findByAppleUserId(appleUserId).orElseGet(() -> {
			User newUser = User.builder()
				.provider("apple")
				.providerId(appleUserId)
				.build();
			return userService.save(newUser);
		});

		// 세션에 사용자 정보 저장
		request.getSession().setAttribute("user", user);

		// 리디렉션 처리
		String redirectUrl = "/";
		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}
}
