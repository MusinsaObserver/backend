package observer.backend.service;

import java.time.LocalDate;
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
      Optional<Product> existingProductOptional = productRepository.findByProductURL(product.getProductURL());
      Optional<PriceHistory> existingPriceHistoryOptional = priceHistoryRepository.findByDate(LocalDate.now());

      if (existingProductOptional.isEmpty()) {
        // 새로운 Product인 경우
        productRepository.save(product); // 새로운 Product 저장
        PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(), product);
        priceHistoryRepository.save(priceHistory); // PriceHistory 저장
      } else {
        // 기존 Product인 경우
        Product existingProduct = existingProductOptional.get();
        existingProduct.update(product);
        productRepository.save(existingProduct);
        if (existingPriceHistoryOptional.isEmpty()) {
          PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(), product);
          priceHistoryRepository.save(priceHistory);
          boolean isPriceDropped = existingProduct.getPrice() > priceHistory.getPrice();
          if (isPriceDropped) {
            likeService.notifyPriceDrop(product.getId());
          }
        }
      }
    }
  }

  public void createPriceHistory(PriceHistory priceHistory, Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT));

    boolean isPriceDropped = product.getPrice() > priceHistory.getPrice();

    priceHistory.setProduct(product);
    priceHistoryRepository.save(priceHistory);

    if (isPriceDropped) {
      likeService.notifyPriceDrop(productId);
    }
  }

  public Page<ProductResponseDto> searchProducts(String query, Pageable pageable) {
    Page<Product> productPage = productRepository.searchByMultipleFields(query, pageable);

    // Using lambda expression to explicitly map Product to ProductResponseDto
    return productPage.map(product -> {
      // Fetch the price history
      List<PriceHistory> fullPriceHistory = priceHistoryRepository.findByProductId(product.getId());
      LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
      List<PriceHistory> threeMonthHistory = fullPriceHistory.stream()
          .filter(history -> history.getDate().isAfter(threeMonthsAgo))
          .toList();

      Integer highestPrice = fullPriceHistory.stream().map(PriceHistory::getPrice).max(Integer::compare).orElse(product.getPrice());
      Integer lowestPrice = fullPriceHistory.stream().map(PriceHistory::getPrice).min(Integer::compare).orElse(product.getPrice());

      Date favoriteDate = null;  // Logic to fetch favoriteDate from the Like entity can be added here

      return ProductResponseDto.fromEntity(product, threeMonthHistory, highestPrice, lowestPrice, favoriteDate);
    });
  }

  public ProductResponseDto searchProduct(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT));

    // Fetch the price history for the product
    List<PriceHistory> fullPriceHistory = priceHistoryRepository.findByProductId(productId);

    // Filter for the last 3 months
    LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
    List<PriceHistory> threeMonthHistory = fullPriceHistory.stream()
        .filter(history -> history.getDate().isAfter(threeMonthsAgo))
        .toList();

    // Calculate highest and lowest prices in the full price history
    Integer highestPrice = fullPriceHistory.stream().map(PriceHistory::getPrice).max(Integer::compare).orElse(product.getPrice());
    Integer lowestPrice = fullPriceHistory.stream().map(PriceHistory::getPrice).min(Integer::compare).orElse(product.getPrice());

    // Find the favorite date if necessary
    Date favoriteDate = null;  // Logic to fetch favoriteDate from the Like entity can be added here

    // Return the DTO with all necessary fields
    return ProductResponseDto.fromEntity(product, threeMonthHistory, highestPrice, lowestPrice, favoriteDate);
  }


  public String getProductCategory(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT));
    return product.getCategory();
  }
}
