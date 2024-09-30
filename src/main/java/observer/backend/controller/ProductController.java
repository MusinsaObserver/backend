package observer.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.response.ApiResponse;
import observer.backend.service.CrawlerService;
import observer.backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

  private final ProductService productService;

  @PostMapping("/crawling")
  public ResponseEntity<?> crawlProduct() {
    productService.crawlProduct();
    return ResponseEntity.ok(ApiResponse.ok("크롤링 및 DB 저장 성공", null));
  }
  @GetMapping("/search")
  public ResponseEntity<?> searchProducts(
      @RequestParam(name = "query") String query,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "100") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<ProductResponseDto> productResponsePage = productService.searchProducts(query, pageable);

    return ResponseEntity.ok(ApiResponse.ok("제품 검색 성공", productResponsePage));
  }

  @GetMapping("/search/{productId}")
  public ResponseEntity<?> searchProduct(@PathVariable Long productId) {
    ProductResponseDto productResponseDto = productService.searchProduct(productId);
    return ResponseEntity.ok(ApiResponse.ok("제품 세부사항 검색 성공", productResponseDto));
  }
}
