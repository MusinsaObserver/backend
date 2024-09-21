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
		Long userId;
		try {
			userId = Long.parseLong(identifier);
		} catch (NumberFormatException e) {
			throw new UsernameNotFoundException("Invalid user ID format: " + identifier);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

		return org.springframework.security.core.userdetails.User.builder()
			.username(String.valueOf(user.getUserId()))
			.password("{noop}")
			.roles("USER")
			.build();
	}
}
