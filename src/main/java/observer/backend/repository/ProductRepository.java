package observer.backend.repository;

import java.util.List;
import java.util.Optional;
import observer.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product,Long> {

  List<Product> findByProductNameContaining(String query);
  List<Product> findTop10ByProductNameContaining(String query);
  Optional<Product> findById(Long productId);

  Optional<Product> findByProductURL(String productURL);
  Page<Product> findByProductNameContaining(String query, Pageable pageable);
  Page<Product> findByCategory(String category, Pageable pageable);
}
