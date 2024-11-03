package observer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import observer.backend.dto.AuthRequestDto;
import observer.backend.dto.AuthResponseDto;
import observer.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/apple/login")
	public ResponseEntity<AuthResponseDto> handleAppleLogin(@RequestBody AuthRequestDto authRequest, HttpServletRequest request) {
		String idToken = authRequest.getIdToken();
		AuthResponseDto response = authService.processAppleLogin(idToken);

		request.getSession().setAttribute("userId", response.getUserId());
		System.out.println("User logged in with ID: " + response.getUserId());

		// 새로운 사용자인지 여부를 응답에 포함
		response.setNewUser(response.isNewUser());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseMessage> logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return ResponseEntity.ok(new ResponseMessage("Successfully logged out"));
	}

	@DeleteMapping("/delete")
	public ResponseEntity<ResponseMessage> deleteAccount(HttpServletRequest request) {
		Long userId = (Long) request.getSession().getAttribute("userId");
		if (userId == null) {
			System.out.println("User ID is null. Unauthorized access.");
			return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized. User not logged in."));
		}

		System.out.println("Deleting user with ID: " + userId);
		boolean isDeleted = authService.deleteUserById(userId);
		if (isDeleted) {
			request.getSession().invalidate();
			return ResponseEntity.ok(new ResponseMessage("Successfully deleted user"));
		} else {
			return ResponseEntity.status(500).body(new ResponseMessage("Failed to delete user"));
		}
	}

	@GetMapping("/public")
	public String publicEndpoint() {
		return "Public endpoint";
	}

	public static class ResponseMessage {
		private String message;

		public ResponseMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}