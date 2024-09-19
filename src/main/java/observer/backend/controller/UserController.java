package observer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
		String email = authentication.getName();
		User user = userService.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return ResponseEntity.ok(new UserDto(user.getUserId(), user.getEmail()));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
		userService.deleteUserById(userId);
		return ResponseEntity.noContent().build();
	}

	// 로그인된 사용자의 이메일 반환
	@GetMapping("/email")
	public ResponseEntity<UserDto> getUserEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		User user = userService.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return ResponseEntity.ok(new UserDto(user.getUserId(), user.getEmail()));
	}

	@GetMapping("/id")
	public Long getUserId() {
		return userService.getUserId();
	}
}
