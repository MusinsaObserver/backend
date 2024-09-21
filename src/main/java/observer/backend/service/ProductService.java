package observer.backend.service;

import java.time.LocalDate;
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
  private final CrawlerService crawlerService;

  public void crawlProduct() {
    List<String[]> crawlingList = crawlerService.parallelCrawling();
  }

  public void createProduct(List<Product> productList) {
    for (Product product : productList) {
      Optional<Product> existingProductOptional = productRepository.findByProductURL(
          product.getProductURL());
      Optional<PriceHistory> existingPriceHistoryOptional = priceHistoryRepository.findByDate(
          LocalDate.now());

      if (existingProductOptional.isPresent()) {
        Product existingProduct = existingProductOptional.get();
        existingProduct.update(product);
        productRepository.save(existingProduct);
        if (existingPriceHistoryOptional.isEmpty()) {
          PriceHistory priceHistory = new PriceHistory(LocalDate.now(), product.getPrice(),
              product);
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
        .orElseThrow(
            () -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT));

    boolean isPriceDropped = product.getPrice() > priceHistory.getPrice();

    priceHistory.setProduct(product);
    priceHistoryRepository.save(priceHistory);

    if (isPriceDropped) {
      likeService.notifyPriceDrop(productId);
    }
  }

  public Page<ProductResponseDto> searchProducts(String query, Pageable pageable) {
    Page<Product> productPage = productRepository.findByProductNameContaining(query, pageable);

    return productPage.map(ProductResponseDto::new);
  }

  public ProductResponseDto searchProduct(Long productId) {
    return new ProductResponseDto(productRepository.findById(productId)
        .orElseThrow(
            () -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT)));
  }

  public String getProductNameById(Long productId) {
    return productRepository.findById(productId)
        .map(Product::getProductName)
        .orElseThrow(
            () -> new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_PRODUCT));
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
