package observer.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.exception.BusinessException;
import observer.backend.exception.ErrorCode;
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
  private final CrawlerService crawlerService;

  public void crawlProduct() {
    List<String[]> crawlingList = crawlerService.parallelCrawling();
    List<Product> productList = new ArrayList<>();
    for (String[] strings : crawlingList) {
      String productCode = strings[0];
      String brand = strings[2];
      String productName = strings[3];
      Integer price = Integer.valueOf(strings[4]);
      Integer discountRate = Integer.valueOf(strings[5]);
      Integer originalPrice = Integer.valueOf(strings[6]);
      String productURL = strings[7];
      String imageURL = strings[8];
      Product product = new Product(productCode, brand, productName, price, discountRate,
          originalPrice, productURL, imageURL);
      productList.add(product);
    }
    createProduct(productList);
  }

  public void createProduct(List<Product> productList) {
    for (Product product : productList) {
      Optional<Product> existingProductOptional = productRepository.findByProductCode(
          product.getProductCode());

      if (existingProductOptional.isEmpty()) {
        // 새로운 Product인 경우
        productRepository.save(product); // 새로운 Product 저장
        PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(), product);
        priceHistoryRepository.save(priceHistory); // PriceHistory 저장
      } else {
        // 기존 Product인 경우
        Product existingProduct = existingProductOptional.get();
        if (!Objects.equals(existingProduct.getPrice(), product.getPrice())) {
          // 가격이 다른 경우
          existingProduct.setPrice(product.getPrice()); // 가격 업데이트
          productRepository.save(existingProduct); // 업데이트된 Product 저장

          // PriceHistory를 저장할 때, existingProduct를 사용
          PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(),
              existingProduct);
          priceHistoryRepository.save(priceHistory); // 새로운 PriceHistory 저장
          likeService.notifyPriceDrop(existingProduct.getId()); // 가격 변화 알림
        }
      }
    }
  }


  public Page<ProductResponseDto> searchProducts(String query, Pageable pageable) {
    Page<Product> productPage = productRepository.findByProductNameContaining(query, pageable);

    return productPage.map(ProductResponseDto::new);
  }

  public ProductResponseDto searchProduct(Long productId) {
    return new ProductResponseDto(productRepository.findById(productId).orElseThrow(
        () -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT)));
  }


}
