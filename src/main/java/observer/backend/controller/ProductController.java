package observer.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.response.ApiResponse;
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

  // 테스트 하려고 넣음 실제로는 파이썬에서 db에 제품 정보 넣을 듯
  @PostMapping("/create")
  public ResponseEntity<?> createProduct(@RequestBody List<Product> productList) {
    productService.createProduct(productList);
    return ResponseEntity.ok(ApiResponse.ok("제품 정보 생성 성공", null));
  }

  @PostMapping("/createPriceHistory/{productId}")
  public ResponseEntity<?> createPriceHistory(@RequestBody PriceHistory priceHistory,
      @PathVariable(name = "productId") Long productId) {
    productService.createPriceHistory(priceHistory, productId);
    return ResponseEntity.ok(ApiResponse.ok("가격 변동 정보 생성 성공", null));
  }

  @GetMapping("/autoComplete")
  public ResponseEntity<?> autoComplete(@RequestParam(name = "query") String query){
    List<String> autoCompleteList = productService.autoComplete(query);
    System.out.println(autoCompleteList);
    return ResponseEntity.ok(ApiResponse.ok("자동 완성 성공",autoCompleteList));
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

//  @GetMapping("/{productId}/recommendations")
//  public ResponseEntity<String> getRecommendations(@PathVariable Long productId) {
//    // productId로 제품명 조회
//    String productName = productService.getProductNameById(productId);
//
//    // 추천 시스템 호출
//    String recommendations = pythonService.getRecommendations(productName);
//    return ResponseEntity.ok(recommendations);
//  }
  @GetMapping("/{productId}/category")
  public ResponseEntity<?> getProductCategory(@PathVariable Long productId) {
    String category = productService.getProductCategory(productId);
    return ResponseEntity.ok(ApiResponse.ok("제품 카테고리 조회 성공", category));
  }

}
