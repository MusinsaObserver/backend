package observer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	// 로그아웃 요청 처리
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		request.getSession().invalidate(); // 세션 무효화
		return ResponseEntity.ok("Successfully logged out");
	}

	// 공개 엔드포인트
	@GetMapping("/public")
	public String publicEndpoint() {
		return "Public endpoint";
	}
}

