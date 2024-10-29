package observer.backend.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
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

  public void createProduct(List<Product> productList) {
    for (Product product : productList) {
      Optional<Product> existingProductOptional = productRepository.findByProductURL(product.getProductURL());
      Optional<PriceHistory> existingPriceHistoryOptional = priceHistoryRepository.findByDate(LocalDate.now());

      if (existingProductOptional.isPresent()) {
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
      } else {
        productRepository.save(product);
        PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(), product);
        priceHistoryRepository.save(priceHistory);
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

  public List<String> autoComplete(String query) {
    return productRepository.findTop10ByProductNameContaining(query).stream()
        .map(Product::getProductName).toList();
  }
}
