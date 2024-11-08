package observer.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import observer.backend.entity.User;
import observer.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserService userService;

	public OAuth2LoginSuccessHandler(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String appleUserId = (String) oAuth2User.getAttribute("sub");

		log.info("Attempting to find or create user with Apple ID: {}", appleUserId);

		User user = userService.findByAppleUserId(appleUserId).orElseGet(() -> {
			log.info("User with Apple ID {} not found, creating new user.", appleUserId);
			User newUser = User.builder()
				.provider("apple")
				.providerId(appleUserId)
				.build();
			return userService.save(newUser);
		});

		request.getSession().setAttribute("userId", user.getUserId());
		request.getSession().setMaxInactiveInterval(86400);

		log.info("User logged in with ID: {}", user.getUserId());
		log.info("Session ID: {}", request.getSession().getId());
		log.info("Session Max Inactive Interval: {}", request.getSession().getMaxInactiveInterval());

		if (request.getSession(false) == null) {
			log.warn("No session created for user ID {}. Session might be stateless or session management issues could exist.", user.getUserId());
		} else {
			log.info("Session successfully created for user ID {}.", user.getUserId());
		}

		Object sessionUserId = request.getSession().getAttribute("userId");
		if (sessionUserId == null) {
			log.error("Failed to store user ID in session. Session might not be maintained correctly.");
		} else {
			log.info("User ID {} successfully stored in session.", sessionUserId);
		}
	}
}
