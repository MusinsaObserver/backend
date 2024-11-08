package observer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import observer.backend.dto.AuthRequestDto;
import observer.backend.dto.AuthResponseDto;
import observer.backend.response.ApiResponse;
import observer.backend.response.ErrorResponse;
import observer.backend.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;

	@PostMapping("/apple/login")
	public ResponseEntity<ApiResponse<AuthResponseDto>> handleAppleLogin(@RequestBody AuthRequestDto authRequest, HttpServletRequest request) {
		try {
			String idToken = authRequest.getIdToken();
			log.info("Received Apple login request with idToken: {}", idToken);

			AuthResponseDto response = authService.processAppleLogin(idToken);

			request.getSession().setAttribute("userId", response.getUserId());
			log.info("User logged in and session created with userID: {}", response.getUserId());

			response.setNewUser(response.isNewUser());
			log.info("Login response prepared: {}", response);

			return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
		} catch (Exception e) {
			log.error("Error during Apple login", e);
			return ResponseEntity.status(500).body(ApiResponse.fail("Login failed", new ErrorResponse(null, e.getMessage())));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return ResponseEntity.ok(ApiResponse.ok("Successfully logged out", "Logout successful"));
	}

	@DeleteMapping("/delete")
	public ResponseEntity<ApiResponse<String>> deleteAccount(HttpServletRequest request) {
		Long userId = (Long) request.getSession().getAttribute("userId");
		log.info("Received account deletion request for userId: {}", userId);

		if (userId == null) {
			log.warn("Unauthorized attempt to delete account without valid session.");
			return ResponseEntity.status(401).body(ApiResponse.fail("Unauthorized. User not logged in.", null));
		}

		try {
			boolean isDeleted = authService.deleteUserById(userId);
			log.info("User deletion result for userId {}: {}", userId, isDeleted);

			if (isDeleted) {
				request.getSession().invalidate();
				log.info("Session invalidated for userId: {}", userId);
				return ResponseEntity.ok(ApiResponse.ok("Successfully deleted user", "Account deletion successful"));
			} else {
				return ResponseEntity.status(500).body(ApiResponse.fail("Failed to delete user", null));
			}
		} catch (Exception e) {
			log.error("Error during account deletion for userId: {}", userId, e);
			return ResponseEntity.status(500).body(ApiResponse.fail("Error deleting user", new ErrorResponse(null, e.getMessage())));
		}
	}

	@PostMapping("/validate")
	public ResponseEntity<ApiResponse<String>> validateSession(HttpServletRequest request) {
		Long userId = (Long) request.getSession().getAttribute("userId");
		if (userId != null) {
			return ResponseEntity.ok(ApiResponse.ok("Session is valid", "Session validation successful"));
		} else {
			return ResponseEntity.status(401).body(ApiResponse.fail("Session is invalid or expired", null));
		}
	}

	@GetMapping("/public")
	public String publicEndpoint() {
		return "Public endpoint";
	}
}
