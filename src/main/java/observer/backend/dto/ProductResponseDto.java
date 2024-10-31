package observer.backend.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;

@Getter
@AllArgsConstructor
public class ProductResponseDto {
  private Long id;
  private String productCode;
  private String brand;
  private String productName;
  private Integer price;
  private Integer discountRate;
  private Integer originalPrice;
  private String productURL;
  private String imageURL;
  private List<PriceHistory> priceHistoryList;
  private String category;
  private Date favoriteDate;
  private Integer highestPrice;  // Include highest price
  private Integer lowestPrice;   // Include lowest price

  // Existing constructor
  public ProductResponseDto(Product product) {
    this.id = product.getId();
    this.productCode = product.getProductCode();
    this.brand = product.getBrand();
    this.productName = product.getProductName();
    this.price = product.getPrice();
    this.discountRate = product.getDiscountRate();
    this.originalPrice = product.getOriginalPrice();
    this.productURL = product.getProductURL();
    this.imageURL = product.getImageURL();
    this.priceHistoryList = product.getPriceHistoryList();
    this.category = product.getCategory();
    this.favoriteDate = null;  // Default as null if not passed
    this.highestPrice = null;
    this.lowestPrice = null;
  }

  // New overloaded static factory method
  public static ProductResponseDto fromEntity(Product product, List<PriceHistory> priceHistoryList,
      Integer highestPrice, Integer lowestPrice, Date favoriteDate) {
    return new ProductResponseDto(
        product.getId(),
        product.getBrand(),
        product.getProductName(),
        product.getPrice(),
        product.getDiscountRate(),
        product.getOriginalPrice(),
        product.getProductURL(),
        product.getImageURL(),
        priceHistoryList,
        product.getCategory(),
        favoriteDate,
        highestPrice,
        lowestPrice
    );
  }
}
