package observer.backend.dto;

import lombok.Getter;

@Getter
public class AuthResponseDto {
	private Long userId;
	private String sessionToken;

	public AuthResponseDto(Long userId, String sessionToken) {
		this.userId = userId;
		this.sessionToken = sessionToken;
	}
}
