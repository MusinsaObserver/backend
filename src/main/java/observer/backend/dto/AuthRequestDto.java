package observer.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthRequestDto {
	private String idToken;

	public AuthRequestDto(String idToken) {
		this.idToken = idToken;
	}
}