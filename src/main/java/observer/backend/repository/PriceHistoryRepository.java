package observer.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import observer.backend.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  Optional<PriceHistory> findByDate(LocalDate date);

  List<PriceHistory> findByProductId(Long productId);
}
