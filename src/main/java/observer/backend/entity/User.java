package observer.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "[user]")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(name = "provider", nullable = false)
	private String provider; // OAuth2 제공자 이름 (예: "apple")

	@Column(name = "provider_id", unique = true, nullable = false)
	private String providerId; // Apple에서 제공하는 사용자 고유 ID (sub)

	@Column(name = "device_token")
	private String deviceToken;

	@Column(name = "email")
	private String email; // 이메일 필드 추가 (최초 로그인 시에만 저장)
}
