package observer.backend.productServiceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import observer.backend.dto.ProductResponseDto;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;
import observer.backend.exception.BusinessException;
import observer.backend.exception.ErrorCode;
import observer.backend.repository.PriceHistoryRepository;
import observer.backend.repository.ProductRepository;
import observer.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yml")
class ProductServiceTest {

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private PriceHistoryRepository priceHistoryRepository;

  @BeforeEach
  void setUp() {
    // Initialization logic if needed
  }

  @Test
  void testCreateProduct_whenProductIsNew_shouldSaveProductAndPriceHistory() {
    // Given
    List<Product> newProducts = new ArrayList<>();
    Product product = new Product("P001", "TestBrand", "Test Product", 10000, 10, 11000,
        "http://test.url", "http://image.url");
    newProducts.add(product);

    // When
    productService.createProduct(newProducts);

    // Then
    Optional<Product> savedProduct = productRepository.findByProductCode("P001");
    assertTrue(savedProduct.isPresent());
    assertEquals("Test Product", savedProduct.get().getProductName());

    // Check if the price history is saved
    List<PriceHistory> priceHistories = priceHistoryRepository.findByProductId(
        savedProduct.get().getId());
    assertEquals(1, priceHistories.size());
    assertEquals(10000, priceHistories.get(0).getPrice());
    assertEquals(LocalDate.now(), priceHistories.get(0).getDate());
  }

  @Test
  void testCreateProduct_whenProductExistsWithSamePrice_shouldNotSaveNewProductOrPriceHistory() {
    // Given
    List<Product> newProduct1 = new ArrayList<>();
    Product product1 = new Product("P001", "TestBrand", "Test Product", 10000, 10, 11000,
        "http://test.url", "http://image.url");
    newProduct1.add(product1);

    List<Product> newProduct2 = new ArrayList<>();
    Product product2 = new Product("P001", "TestBrand", "Test Product", 10000, 10, 11000,
        "http://test.url", "http://image.url");
    newProduct2.add(product2);

    //When
    productService.createProduct(newProduct1);
    productService.createProduct(newProduct2);

    //Then

    Optional<Product> savedProduct = productRepository.findByProductCode("P001");
    assertTrue(savedProduct.isPresent());
    assertEquals("Test Product", savedProduct.get().getProductName());

    List<PriceHistory> priceHistories = priceHistoryRepository.findByProductId(
        savedProduct.get().getId());
    assertEquals(1, priceHistories.size());
    assertEquals(10000, priceHistories.get(0).getPrice());
    assertEquals(LocalDate.now(), priceHistories.get(0).getDate());
  }

  @Test
  void testCreateProduct_whenProductExistsWithDifferentPrice_shouldUpdateProductAndSaveNewPriceHistory() {
    // Given
    List<Product> newProduct1 = new ArrayList<>();
    Product product1 = new Product("P001", "TestBrand", "Test Product", 10000, 10, 11000,
        "http://test.url", "http://image.url");
    newProduct1.add(product1);

    List<Product> newProduct2 = new ArrayList<>();
    Product product2 = new Product("P001", "TestBrand", "Test Product", 10001, 10, 11000,
        "http://test.url", "http://image.url");
    newProduct2.add(product2);

    // When
    productService.createProduct(newProduct1);
    productService.createProduct(newProduct2);

    // Then
    Product product = productRepository.findByProductCode(product1.getProductCode()).get();
    assertEquals(10001, product.getPrice());

    List<PriceHistory> priceHistories = priceHistoryRepository.findByProductId(
        product.getId());
    assertEquals(2, priceHistories.size());
  }

  @Test
  void testSearchProducts_whenProductsExist_shouldReturnProductResponseDtoPage() {
    // Given
    String query = "Test";
    Pageable pageable = Pageable.ofSize(10);
    Product product = new Product("P001", "TestBrand", "Test Product", 10000, 10, 11000,
        "http://test.url", "http://image.url");
    Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

    when(productRepository.findByProductNameContaining(query, pageable)).thenReturn(productPage);

    // When
    Page<ProductResponseDto> result = productService.searchProducts(query, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Test Product", result.getContent().get(0).getProductName());
  }

  @Test
  void testSearchProduct_whenProductExists_shouldReturnProductResponseDto() {
    // Given
    Long productId = 1L;
    Product product = new Product("P001", "TestBrand", "Test Product", 10000, 10, 11000,
        "http://test.url", "http://image.url");

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    // When
    ProductResponseDto result = productService.searchProduct(productId);

    // Then
    assertNotNull(result);
    assertEquals("Test Product", result.getProductName());
  }

  @Test
  void testSearchProduct_whenProductDoesNotExist_shouldThrowBusinessException() {
    // Given
    Long productId = 1L;

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class,
        () -> productService.searchProduct(productId));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals(ErrorCode.NOT_FOUND_PRODUCT.getMessage(), exception.getErrorCode());
  }

}