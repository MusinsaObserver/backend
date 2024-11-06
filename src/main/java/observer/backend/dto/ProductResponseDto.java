package observer.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import observer.backend.entity.Product;
import observer.backend.entity.PriceHistory;

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
  private List<PriceHistoryDto> priceHistoryList;
  private String category;
  private Date favoriteDate;
  private Integer highestPrice;
  private Integer lowestPrice;
  private Integer currentPrice;

  // Constructor using Product entity and additional data
  public ProductResponseDto(Product product, List<PriceHistory> priceHistories,
      Integer highestPrice, Integer lowestPrice, Date favoriteDate) {
    this.id = product.getId();
    this.productCode = product.getProductCode();
    this.brand = product.getBrand();
    this.productName = product.getProductName();
    this.price = product.getPrice();
    this.discountRate = product.getDiscountRate();
    this.originalPrice = product.getOriginalPrice();
    this.productURL = product.getProductURL();
    this.imageURL = product.getImageURL();
    this.category = product.getCategory();
    this.favoriteDate = favoriteDate;
    this.highestPrice = highestPrice;
    this.lowestPrice = lowestPrice;
    this.currentPrice = product.getPrice();
    this.priceHistoryList = priceHistories.stream()
        .map(PriceHistoryDto::new) // Convert PriceHistory entities to PriceHistoryDto objects
        .collect(Collectors.toList());
  }

  // Static factory method for creating ProductResponseDto from a Product entity
  public static ProductResponseDto fromEntity(Product product, List<PriceHistory> priceHistoryList,
      Integer highestPrice, Integer lowestPrice, Date favoriteDate) {
    return new ProductResponseDto(
        product,
        priceHistoryList,
        highestPrice,
        lowestPrice,
        favoriteDate
    );
  }
}
