package observer.backend.service;

import observer.backend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import observer.backend.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    
		// userId를 사용하여 사용자 조회
		Long userId;
		try {
			userId = Long.parseLong(identifier); // identifier를 Long 타입의 userId로 변환
		} catch (NumberFormatException e) {
			throw new UsernameNotFoundException("Invalid user ID format: " + identifier);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

		// Spring Security의 UserDetails 객체 생성
		return org.springframework.security.core.userdetails.User.builder()
			.username(String.valueOf(user.getUserId())) // userId를 username으로 설정
			.password("{noop}") // 비밀번호 인코딩 필요 시 적용
			.roles("USER") // 필요한 경우 역할 설정
			.build();
	}
}
