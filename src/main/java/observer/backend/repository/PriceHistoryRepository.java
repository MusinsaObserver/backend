package observer.backend.repository;

import java.time.LocalDate;
import java.util.Optional;
import observer.backend.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory,Long> {

  Optional<PriceHistory> findByDate(LocalDate date);
}
