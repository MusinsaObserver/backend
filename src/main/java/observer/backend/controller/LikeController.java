package observer.backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.Product;
import observer.backend.response.ApiResponse;
import observer.backend.response.ErrorResponse;
import observer.backend.service.LikeService;
import observer.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Slf4j
public class LikeController {

	private final LikeService likeService;
  
	private final UserService userService;

	@PostMapping("/product/{productId}")
	public ResponseEntity<ApiResponse<String>> likeProduct(@PathVariable Long productId, Authentication authentication) {
		try {
			Long userId = getUserIdFromAuthentication(authentication);
			log.info("User {} is attempting to like product {}", userId, productId);

			// 로그 추가: 세션 정보
			logSessionDetails(authentication);

			likeService.likeProduct(userId, productId);

			log.info("Product {} liked by user {}", productId, userId);
			return ResponseEntity.ok(ApiResponse.ok("Product liked successfully", "Like successful"));
		} catch (Exception e) {
			log.error("Error while liking product {}", productId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to like product", new ErrorResponse(null, e.getMessage())));
		}
	}

	@DeleteMapping("/product/{productId}")
	public ResponseEntity<ApiResponse<String>> unlikeProduct(@PathVariable Long productId, Authentication authentication) {
		try {
			Long userId = getUserIdFromAuthentication(authentication);
			log.info("User {} is attempting to unlike product {}", userId, productId);

			// 로그 추가: 세션 정보
			logSessionDetails(authentication);

			likeService.unlikeProduct(userId, productId);

			log.info("Product {} unliked by user {}", productId, userId);
			return ResponseEntity.ok(ApiResponse.ok("Product unliked successfully", "Unlike successful"));
		} catch (Exception e) {
			log.error("Error while unliking product {}", productId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to unlike product", new ErrorResponse(null, e.getMessage())));
		}
	}

	@GetMapping("/my")
	public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getLikedProductsByUser(Authentication authentication) {
		try {
			Long userId = getUserIdFromAuthentication(authentication);
			log.info("Retrieving liked products for user {}", userId);

			// 로그 추가: 세션 정보
			logSessionDetails(authentication);

			List<Product> likedProducts = likeService.getLikedProductsByUser(userId);
			List<ProductResponseDto> productDtos = likedProducts.stream()
				.map(product -> ProductResponseDto.fromEntity(product, product.getPriceHistoryList(), null, null, null))
				.collect(Collectors.toList());

			log.info("Retrieved {} liked products for user {}", productDtos.size(), userId);
			return ResponseEntity.ok(ApiResponse.ok("Successfully retrieved liked products", productDtos));
		} catch (Exception e) {
			log.error("Failed to retrieve liked products for user", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to retrieve liked products", new ErrorResponse(null, e.getMessage())));
		}
	}

	@GetMapping("/status/{productId}")
	public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(@PathVariable Long productId, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			log.warn("Unauthorized access attempt to check like status for product {}", productId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHORIZED", null));
		}
		try {
			Long userId = getUserIdFromAuthentication(authentication);
			log.info("Checking like status for product {} by user {}", productId, userId);

			// 로그 추가: 세션 정보
			logSessionDetails(authentication);

			boolean isLiked = likeService.isLikedByUser(userId, productId);

			log.info("Like status for product {} by user {}: {}", productId, userId, isLiked);
			return ResponseEntity.ok(ApiResponse.ok("Successfully checked like status", isLiked));
		} catch (Exception e) {
			log.error("Error checking like status for product {}", productId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to check like status", new ErrorResponse(null, e.getMessage())));
		}
	}

	// 유틸리티 메서드: 인증에서 사용자 ID 추출
	private Long getUserIdFromAuthentication(Authentication authentication) {
		try {
			OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
			String providerId = (String) oAuth2User.getAttribute("sub");
			Long userId = userService.findByAppleUserId(providerId)
				.orElseThrow(() -> new RuntimeException("User not found"))
				.getUserId();

			log.info("Retrieved user ID {} from authentication with provider ID {}", userId, providerId);
			return userId;
		} catch (Exception e) {
			log.error("Error retrieving user from authentication", e);
			throw new RuntimeException("Error retrieving user from authentication", e);
		}
	}

	// 유틸리티 메서드: 세션 정보 로그 추가
	private void logSessionDetails(Authentication authentication) {
		if (authentication == null) {
			log.warn("Authentication is null - unable to log session details.");
			return;
		}
		try {
			log.info("Session ID from request: {}", authentication.getDetails());
		} catch (Exception e) {
			log.error("Error logging session details", e);
		}
	}
}
