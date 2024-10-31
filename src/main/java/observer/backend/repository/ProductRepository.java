package observer.backend.repository;

import java.util.List;
import java.util.Optional;
import observer.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  // 자동완성 기능
  List<Product> findTop10ByProductNameContaining(String query);

  // ID로 제품 찾기
  Optional<Product> findById(Long productId);

  // URL로 제품 찾기
  Optional<Product> findByProductURL(String productURL);

  // 여러 필드(product_name, category, brand)에서 검색
  @Query("SELECT p FROM Product p WHERE p.productName LIKE %:query% OR p.category LIKE %:query% OR p.brand LIKE %:query%")
  Page<Product> searchByMultipleFields(@Param("query") String query, Pageable pageable);
}
