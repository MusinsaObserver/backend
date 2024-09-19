package observer.backend.security.

import org.springframework.security.core.Authentication;
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
		// 인증된 사용자 정보 가져오기
		String email = authentication.getName();
		User user = userService.findByEmail(email).orElseGet(() -> {
			User newUser = User.builder()
				.email(email)
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
