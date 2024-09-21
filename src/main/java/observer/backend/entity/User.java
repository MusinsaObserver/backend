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

	@Column(name = "provider")
	private String provider; // OAuth2 제공자 이름

	@Column(name = "provider_id")
	private String providerId; // OAuth2 사용자 ID

	@Column(name = "device_token")
	private String deviceToken;
}
