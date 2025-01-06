package observer.backend.repository;

import java.util.Optional;
import observer.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {

  Optional<Category> findByName(String categoryName);
}
