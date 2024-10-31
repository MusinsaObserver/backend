package observer.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import observer.backend.dto.AuthResponseDto;
import observer.backend.entity.User;
import observer.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RestTemplate restTemplate;

	@Value("${apple.oauth2.revoke-token-uri}")
	private String appleRevokeTokenUri;

	@Value("${apple.client-id}")
	private String appleClientId;

	@Value("${apple.client-secret}")
	private String appleClientSecret;

	public AuthService(UserRepository userRepository, RestTemplate restTemplate) {
		this.userRepository = userRepository;
		this.restTemplate = restTemplate;
	}

	public AuthResponseDto processAppleLogin(String idToken) {
		String appleUserId = extractAppleUserIdFromToken(idToken);

		User user = userRepository.findByProviderId(appleUserId)
			.orElseGet(() -> {
				User newUser = User.builder()
					.provider("apple")
					.providerId(appleUserId)
					.build();
				return userRepository.save(newUser);
			});

		return new AuthResponseDto(user.getUserId(), "sessionToken");
	}

	private String extractAppleUserIdFromToken(String idToken) {
		try {
			PublicKey publicKey = getApplePublicKey(idToken);

			Claims claims = Jwts.parserBuilder()
				.setSigningKey(publicKey) // 공개 키 사용
				.build()
				.parseClaimsJws(idToken)
				.getBody();

			return claims.getSubject(); // 'sub' 값 반환 (Apple 사용자 고유 식별자)
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Invalid JWT Token");
		}
	}

	private PublicKey getApplePublicKey(String idToken) {
		try {
			// JWT 헤더에서 kid 값을 추출 (서명 검증 없이)
			String[] tokenParts = idToken.split("\\.");
			String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode header = objectMapper.readTree(headerJson);
			String kid = header.get("kid").asText();

			// Apple 공개 키를 가져오는 URL
			URL url = new URL("https://appleid.apple.com/auth/keys");
			InputStream inputStream = url.openStream();

			JsonNode jsonNode = objectMapper.readTree(inputStream);
			JsonNode keys = jsonNode.get("keys");

			JsonNode key = null;
			for (JsonNode k : keys) {
				if (k.get("kid").asText().equals(kid)) {
					key = k;
					break;
				}
			}

			if (key == null) {
				throw new RuntimeException("Failed to find matching key");
			}

			String nStr = key.get("n").asText();
			String eStr = key.get("e").asText();

			byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
			byte[] eBytes = Base64.getUrlDecoder().decode(eStr);

			BigInteger modulus = new BigInteger(1, nBytes);
			BigInteger exponent = new BigInteger(1, eBytes);

			// 공개 키 생성
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
			return keyFactory.generatePublic(spec);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to retrieve Apple public key");
		}
	}

	@Transactional
	public boolean deleteUserById(Long userId) {
		try {
			Optional<User> userOptional = userRepository.findById(userId);
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				revokeAppleToken(user.getProviderId());
				userRepository.delete(user);
				return true;
			} else {
				return false; // 사용자를 찾지 못함
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void revokeAppleToken(String providerId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", appleClientId);
		body.add("client_secret", appleClientSecret);
		body.add("token", providerId);

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(appleRevokeTokenUri, requestEntity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("Failed to revoke Apple token");
		}
	}
}
