package observer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import observer.backend.entity.Like;
import observer.backend.entity.Product;
import observer.backend.entity.User;

public interface LikeRepository extends JpaRepository<Like, Long> {

	// 특정 사용자와 특정 상품 간의 찜 관계 조회
	Optional<Like> findByUserAndProduct(User user, Product product);

	// 특정 사용자가 찜한 모든 상품 조회
	List<Like> findAllByUser(User user);

	// 특정 상품을 찜한 모든 사용자 조회
	List<Like> findAllByProduct(Product product);
}
