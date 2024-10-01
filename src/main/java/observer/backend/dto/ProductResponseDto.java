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
  private String productCode;
  private String brand;
  private String productName;
  private Integer price;
  private Integer discountRate;
  private Integer originalPrice;
  private String productURL;
  private String imageURL;
  private List<PriceHistory> priceHistoryList;

  public ProductResponseDto(Product product){
    this.id = product.getId();
    this.productCode = product.getProductCode();
    this.brand = product.getBrand();
    this.productName = product.getProductName();
    this.price = product.getPrice();
    this.discountRate = product.getDiscountRate();
    this.originalPrice = product.getOriginalPrice();
    this.productURL = product.getProductURL();
    this.priceHistoryList = product.getPriceHistoryList();
    this.imageURL = product.getImageURL();
  }
}