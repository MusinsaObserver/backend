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

	// 사용자 정보를 저장하는 메소드
	public User save(User user) {
		return userRepository.save(user);
	}

	// 이메일을 통해 사용자 정보를 찾는 메소드 (Optional 반환)
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// 사용자 ID를 통해 사용자를 삭제하는 메서드 추가
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
