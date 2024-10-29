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

	@Column(name = "provider", nullable = false)
	private String provider;

	@Column(name = "provider_id", unique = true, nullable = false)
	private String providerId; // Apple에서 제공하는 사용자 고유 ID (sub)

	@Column(name = "device_token")
	private String deviceToken;
}
