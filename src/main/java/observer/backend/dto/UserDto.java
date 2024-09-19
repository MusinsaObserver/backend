package observer.backend.dto;

import lombok.Getter;

@Getter
public class UserDto {

	private Long id;
	private String email;

	public UserDto() {
	}

	public UserDto(Long id, String email) {
		this.id = id;
		this.email = email;
	}
}
