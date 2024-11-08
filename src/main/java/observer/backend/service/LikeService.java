package observer.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import observer.backend.entity.Like;
import observer.backend.entity.Product;
import observer.backend.entity.User;
import observer.backend.exception.BusinessException;
import observer.backend.repository.LikeRepository;
import observer.backend.repository.ProductRepository;
import observer.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LikeService {

	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final PushNotificationService pushNotificationService;
	private static final int NOTIFICATION_INTERVAL_DAYS = 3;

	public LikeService(LikeRepository likeRepository, UserRepository userRepository,
		ProductRepository productRepository, PushNotificationService pushNotificationService) {
		this.likeRepository = likeRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.pushNotificationService = pushNotificationService;
	}

	@Transactional
	public void likeProduct(Long userId, Long productId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException.UserNotFoundException());
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException.ProductNotFoundException());

		Optional<Like> existingLike = likeRepository.findByUserAndProduct(user, product);
		if (existingLike.isPresent()) {
			throw new BusinessException.ProductAlreadyLikedException();
		}

		Like like = Like.builder()
			.user(user)
			.product(product)
			.initialPrice(product.getPrice())
			.build();

		likeRepository.save(like);
	}

	@Transactional
	public void unlikeProduct(Long userId, Long productId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException.UserNotFoundException());
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException.ProductNotFoundException());

		Like like = likeRepository.findByUserAndProduct(user, product)
			.orElseThrow(() -> new BusinessException.LikeNotFoundException());

		likeRepository.delete(like);
	}

	public List<Product> getLikedProductsByUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException.UserNotFoundException());

		List<Like> likes = likeRepository.findAllByUser(user);
		return likes.stream()
			.map(Like::getProduct)
			.collect(Collectors.toList());
	}

	@Transactional
	public void notifyPriceDrop(Long productId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException.ProductNotFoundException());

		List<Like> likes = likeRepository.findAllByProduct(product);

		for (Like like : likes) {
			if (product.getPrice() < like.getInitialPrice()) {
				boolean shouldSendNotification = true;
				if (like.getLastNotificationTime() != null) {
					LocalDateTime now = LocalDateTime.now();
					shouldSendNotification = like.getLastNotificationTime().plusDays(NOTIFICATION_INTERVAL_DAYS).isBefore(now);
				}

				if (shouldSendNotification) {
					sendPriceDropNotification(like.getUser(), product, like.getInitialPrice());
					like.setLastNotificationTime(LocalDateTime.now());
					likeRepository.save(like);
				}
			}
		}
	}

	private void sendPriceDropNotification(User user, Product product, int initialPrice) {
		String title = "찜한 상품의 가격이 하락했습니다!";
		String body = String.format("상품 '%s'의 가격이 하락했습니다. 현재 가격: %d원", product.getProductName(), product.getPrice());

		pushNotificationService.sendNotification(user.getDeviceToken(), title, body);
	}
	@Transactional(readOnly = true)
	public boolean isLikedByUser(Long userId, Long productId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException.UserNotFoundException());
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException.ProductNotFoundException());

		Optional<Like> like = likeRepository.findByUserAndProduct(user, product);
		return like.isPresent();
	}
}
