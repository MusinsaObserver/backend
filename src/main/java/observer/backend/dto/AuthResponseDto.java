package observer.backend.dto;

import lombok.Getter;

@Getter
public class AuthResponseDto {
	private Long userId;
	private String sessionToken;
	private boolean isNewUser;

	public AuthResponseDto(Long userId, String sessionToken) {
		this.userId = userId;
		this.sessionToken = sessionToken;
	}

	public boolean isNewUser() {
		return isNewUser;
	}

	public void setNewUser(boolean newUser) {
		this.isNewUser = newUser;
	}
}
