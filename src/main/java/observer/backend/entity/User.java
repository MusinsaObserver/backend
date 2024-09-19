package observer.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "provider")
	private String provider; // OAuth2 제공자 이름

	@Column(name = "provider_id")
	private String providerId; // OAuth2 사용자 ID

	// 테스트용 생성자
	public User(String email) {
		this.email = email;
	}
}
