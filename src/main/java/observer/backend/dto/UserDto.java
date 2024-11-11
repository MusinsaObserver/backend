package observer.backend.dto;

import lombok.Getter;

@Getter
public class UserDto {

	private Long id;

	public UserDto() {
	}

	public UserDto(Long id) {
		this.id = id;
	}
}
