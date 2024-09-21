package observer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import observer.backend.dto.UserDto;
import observer.backend.entity.User;
import observer.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// 사용자 정보 조회
	@GetMapping("/profile")
	public ResponseEntity<UserDto> getUserProfile(Authentication authentication) {
		String providerId = getProviderIdFromAuthentication(authentication);
		User user = userService.findByAppleUserId(providerId)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return ResponseEntity.ok(new UserDto(user.getUserId(), user.getProviderId()));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
		userService.deleteUserById(userId);
		return ResponseEntity.noContent().build();
	}

	// 로그인된 사용자의 providerId 반환
	@GetMapping("/providerId")
	public ResponseEntity<UserDto> getUserProviderId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String providerId = getProviderIdFromAuthentication(authentication);
		User user = userService.findByAppleUserId(providerId)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return ResponseEntity.ok(new UserDto(user.getUserId(), user.getProviderId()));
	}

	@GetMapping("/id")
	public Long getUserId() {
		return userService.getUserId();
	}

	// Authentication에서 providerId를 가져오는 메서드
	private String getProviderIdFromAuthentication(Authentication authentication) {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		return (String) oAuth2User.getAttribute("sub"); // Apple의 고유 식별자
	}
}
