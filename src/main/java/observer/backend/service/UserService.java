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

	public User save(User user) {
		return userRepository.save(user);
	}

	public Optional<User> findByAppleUserId(String providerId) {
		return userRepository.findByProviderId(providerId);
	}

	public Long getUserId() {
		User user = userRepository.findFirstByOrderByUserIdAsc()
			.orElseThrow(() -> new IllegalStateException("No users found in the database"));
		return user.getUserId();
	}
}
