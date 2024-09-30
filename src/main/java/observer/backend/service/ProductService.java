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
      if (existingProductOptional.isEmpty()) { // 만약 product가 존재하지 않는다면 product, priceHistory 저장
        productRepository.save(product);
        PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(), product);
        priceHistoryRepository.save(priceHistory);
      } else { // 이미 product가 존재한다면 priceHistory 검색
        PriceHistory priceHistory = priceHistoryRepository.findByProductId(
            existingProductOptional.get().getId());
        if (!Objects.equals(priceHistory.getPrice(),
            product.getPrice())) { // priceHistory의 가격과 product의 가격이 다를 경우 새로운 priceHistory 저장
          priceHistoryRepository.save(
              new PriceHistory(LocalDate.now(), product.getPrice(), product));
          likeService.notifyPriceDrop(product.getId());
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
