package observer.backend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import observer.backend.entity.PriceHistory;
import observer.backend.entity.Product;


@Getter
@AllArgsConstructor
public class ProductResponseDto {
  private Long id;
  private String brand;
  private String productName;
  private Integer price;
  private String discountRate;
  private Integer originalPrice;
  private String productURL;
  private String imageURL;
  private List<PriceHistory> priceHistoryList;
  private String category;

  public ProductResponseDto(Product product){
    this.id = product.getId();
    this.brand = product.getBrand();
    this.productName = product.getProductName();
    this.price = product.getPrice();
    this.discountRate = product.getDiscountRate();
    this.originalPrice = product.getOriginalPrice();
    this.productURL = product.getProductURL();
    this.priceHistoryList = product.getPriceHistoryList();
    this.imageURL = product.getImageURL();
    this.category = product.getCategory();
  }
}