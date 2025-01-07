package observer.backend.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.Category;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.exception.BusinessException;
import observer.backend.exception.ErrorCode;
import observer.backend.repository.CategoryRepository;
import observer.backend.repository.PriceHistoryRepository;
import observer.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final PriceHistoryRepository priceHistoryRepository;
  private final LikeService likeService;
  private final CategoryRepository categoryRepository;


  @Transactional
  public void createProduct(List<String[]> productList) {
    for (String[] strings : productList) {
      String productCode = strings[0];
      String brand = strings[2];
      String productName = strings[3];
      Integer price = Integer.valueOf(strings[4]);
      Integer discountRate = Integer.valueOf(strings[5]);
      Integer originalPrice = Integer.valueOf(strings[6]);
      String productURL = strings[7];
      String imageURL = strings[8];
      String categoryName = strings[1]; // 임시

      // Step 1: 제품 저장 또는 업데이트
      Product product = productRepository.findByProductCode(productCode)
          .orElseGet(() -> {
            // Product 저장
            Product newProduct = new Product(productCode, brand, productName, price, discountRate,
                originalPrice, productURL, imageURL);
            productRepository.save(newProduct);

            // PriceHistory 저장 (Product와 연관 설정)
            PriceHistory priceHistory = new PriceHistory(LocalDate.now(), price, newProduct);
            priceHistoryRepository.save(priceHistory);

            // Product 반환
            return newProduct;
          });

      // Step 2: 가격 변동 체크 및 기록
      if (!product.getPrice().equals(price)) {
        // 가격 변동이 있을 경우 PriceHistory 저장
        PriceHistory priceHistory = new PriceHistory(LocalDate.now(), price, product);
        priceHistoryRepository.save(priceHistory);

        // Product 테이블 업데이트
        product.setPrice(price);
        productRepository.save(product);
      }

      // Step 3: 카테고리 연관 추가(이미 product가 존재하는 경우)
      Category category = categoryRepository.findByName(categoryName)
          .orElseGet(() -> {
            Category newCategory = new Category(categoryName);
            return categoryRepository.save(newCategory);
          });

      if (!product.getCategories().contains(category)) {
        product.getCategories().add(category);
        productRepository.save(product);
      }
    }
  }

  public Page<ProductResponseDto> searchProducts(String query, Pageable pageable) {
    Page<Product> productPage = productRepository.searchByMultipleFields(query, pageable);

    return productPage.map(product -> {
      LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
      List<PriceHistory> threeMonthHistory = priceHistoryRepository.findByProductIdAndDateBetween(
          product.getId(), threeMonthsAgo, LocalDate.now());

      Integer highestPrice = threeMonthHistory.stream().map(PriceHistory::getPrice)
          .max(Integer::compare).orElse(product.getPrice());
      Integer lowestPrice = threeMonthHistory.stream().map(PriceHistory::getPrice)
          .min(Integer::compare).orElse(product.getPrice());

      Date favoriteDate = null;

      return ProductResponseDto.fromEntity(product, threeMonthHistory, highestPrice, lowestPrice,
          favoriteDate);
    });
  }

  public ProductResponseDto searchProduct(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(
            () -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT));

    LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
    List<PriceHistory> threeMonthHistory = priceHistoryRepository.findByProductIdAndDateBetween(
        productId, threeMonthsAgo, LocalDate.now());

    Integer highestPrice = threeMonthHistory.stream().map(PriceHistory::getPrice)
        .max(Integer::compare).orElse(product.getPrice());
    Integer lowestPrice = threeMonthHistory.stream().map(PriceHistory::getPrice)
        .min(Integer::compare).orElse(product.getPrice());

    Date favoriteDate = null;

    return ProductResponseDto.fromEntity(product, threeMonthHistory, highestPrice, lowestPrice,
        favoriteDate);
  }
}
