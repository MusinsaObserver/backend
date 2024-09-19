package observer.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.Product;
import observer.backend.entity.User;
import observer.backend.response.ApiResponse;
import observer.backend.service.LikeService;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;

	// 특정 상품을 찜하기
	@PostMapping("/product/{productId}")
	public ResponseEntity<ApiResponse<?>> likeProduct(@PathVariable Long productId, Authentication authentication) {
		Long userId = getUserIdFromAuthentication(authentication);
		likeService.likeProduct(userId, productId);
		return ResponseEntity.ok(ApiResponse.ok("상품 찜하기 성공", null));
	}

	// 특정 상품 찜 해제
	@DeleteMapping("/product/{productId}")
	public ResponseEntity<ApiResponse<?>> unlikeProduct(@PathVariable Long productId, Authentication authentication) {
		Long userId = getUserIdFromAuthentication(authentication);
		likeService.unlikeProduct(userId, productId);
		return ResponseEntity.ok(ApiResponse.ok("상품 찜 해제 성공", null));
	}

	// 사용자가 찜한 상품 조회
	@GetMapping("/my")
	public ResponseEntity<?> getLikedProductsByUser(Authentication authentication) {
		Long userId = getUserIdFromAuthentication(authentication);
		List<Product> likedProducts = likeService.getLikedProductsByUser(userId);
		List<ProductResponseDto> productDtos = likedProducts.stream()
			.map(ProductResponseDto::new)
			.collect(Collectors.toList());
		return ResponseEntity.ok(ApiResponse.ok("찜한 상품 조회 성공", productDtos));
	}

	// Authentication에서 userId를 가져오는 유틸리티 메서드
	private Long getUserIdFromAuthentication(Authentication authentication) {
		String email = authentication.getName();
		User user = userService.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return user.getUserId();
	}
}
