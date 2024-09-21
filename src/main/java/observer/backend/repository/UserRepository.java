package observer.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import observer.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderId(String providerId); // Apple providerId를 통해 사용자 조회
	Optional<User> findFirstByOrderByUserIdAsc();
}
