package observer.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.response.ApiResponse;
import observer.backend.response.ErrorResponse;
import observer.backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Slf4j
public class ProductController {

  private final ProductService productService;

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<ProductResponseDto>>> searchProducts(
      @RequestParam(name = "query") String query,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "100") int size) {
    try {
      log.info("Received product search request with query: {}, page: {}, size: {}", query, page, size);

      Pageable pageable = PageRequest.of(page, size);
      Page<ProductResponseDto> productResponsePage = productService.searchProducts(query, pageable);

      log.info("Returning product search results for query: {}", query);
      return ResponseEntity.ok(ApiResponse.ok("제품 검색 성공", productResponsePage));
    } catch (Exception e) {
      log.error("Error during product search with query: {}", query, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("서버 에러 발생", new ErrorResponse(null, e.getMessage())));
    }
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ApiResponse<ProductResponseDto>> searchProduct(@PathVariable Long productId) {
    log.info("Searching product details for productId: {}", productId);
    ProductResponseDto productResponseDto = productService.searchProduct(productId);
    return ResponseEntity.ok(ApiResponse.ok("제품 세부사항 검색 성공", productResponseDto));
  }
}
