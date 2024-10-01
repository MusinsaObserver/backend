package observer.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.response.ApiResponse;
import observer.backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Slf4j
public class ProductController {

  private final ProductService productService;

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<Void>> createProduct(@RequestBody List<Product> productList) {
    productService.createProduct(productList);
    return ResponseEntity.ok(ApiResponse.ok("제품 정보 생성 성공", (Void) null));
  }

  @PostMapping("/createPriceHistory/{productId}")
  public ResponseEntity<ApiResponse<Void>> createPriceHistory(@RequestBody PriceHistory priceHistory,
      @PathVariable(name = "productId") Long productId) {
    productService.createPriceHistory(priceHistory, productId);
    return ResponseEntity.ok(ApiResponse.ok("가격 변동 정보 생성 성공", (Void) null));
  }


  @GetMapping("/autoComplete")
  public ResponseEntity<?> autoComplete(@RequestParam(name = "query") String query){
    List<String> autoCompleteList = productService.autoComplete(query);
    System.out.println(autoCompleteList);
    return ResponseEntity.ok(ApiResponse.ok("자동 완성 성공",autoCompleteList));
  }

  @GetMapping("/search")
  public ResponseEntity<?> searchProducts(@RequestParam(name = "query") String query,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "100") int size) {
    try {
      log.info("Searching products with query: {}, page: {}, size: {}", query, page, size);
      Pageable pageable = PageRequest.of(page, size);
      Page<ProductResponseDto> productResponsePage = productService.searchProducts(query, pageable);
      return ResponseEntity.ok(ApiResponse.ok("제품 검색 성공", productResponsePage));
    } catch (Exception e) {
      log.error("Error during product search: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("서버 에러 발생", e.getMessage()));
    }
  }

  @GetMapping("/{productId}")
  public ResponseEntity<?> searchProduct(@PathVariable Long productId) {
    log.info("Searching product details for productId: {}", productId);
    ProductResponseDto productResponseDto = productService.searchProduct(productId);
    return ResponseEntity.ok(ApiResponse.ok("제품 세부사항 검색 성공", productResponseDto));
  }

  @GetMapping("/{productId}/category")
  public ResponseEntity<?> getProductCategory(@PathVariable Long productId) {
    String category = productService.getProductCategory(productId);
    return ResponseEntity.ok(ApiResponse.ok("제품 카테고리 조회 성공", category));
  }
}
