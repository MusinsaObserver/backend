package observer.backend.service;

import org.springframework.stereotype.Service;
import java.util.Optional;
import observer.backend.entity.User;
import observer.backend.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// 사용자 정보를 저장하는 메서드
	public User save(User user) {
		return userRepository.save(user);
	}

	// providerId를 통해 Apple 사용자를 찾는 메서드
	public Optional<User> findByAppleUserId(String providerId) {
		return userRepository.findByProviderId(providerId);
	}

	// 사용자 ID를 통해 사용자를 삭제하는 메서드
	public void deleteUserById(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
		userRepository.delete(user);
	}

	public Long getUserId() {
		User user = userRepository.findFirstByOrderByUserIdAsc()
			.orElseThrow(() -> new IllegalStateException("No users found in the database"));
		return user.getUserId();
	}
}
