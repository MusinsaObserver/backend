package observer.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import observer.backend.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  Optional<PriceHistory> findByDate(LocalDate date);

  List<PriceHistory> findByProductId(Long productId);

  List<PriceHistory> findByProductIdAndDateBetween(Long productId, LocalDate startDate, LocalDate endDate);
}
