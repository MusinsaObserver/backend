package observer.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = false)
  private String brand;

  @Column(nullable = false, unique = true)
  private String productCode;

  @Column(nullable = false, unique = false)
  private String productName;

  @Column(nullable = false, unique = false)
  private Integer price;

  @Column(nullable = false, unique = false)
  private String discountRate;

  @Column(nullable = false, unique = false)
  private Integer originalPrice;

  @Column(nullable = false, unique = false)
  private String productURL;

  @Column(nullable = false, unique = false)
  private String imageURL;

  @Column(nullable = false, unique = false)
  private String category;

  @OneToMany(mappedBy = "product")
  List<PriceHistory> priceHistoryList = new ArrayList<>();

  public void update(Product product) {
    this.productCode = product.getProductCode();
    this.brand = product.getBrand();
    this.productName= product.getProductName();
    this.category = product.getCategory();
    this.price = product.getPrice();
    this.originalPrice = product.getOriginalPrice();
    this.discountRate = product.getDiscountRate();
    this.imageURL = product.getImageURL();
    this.productURL = product.getProductURL();
  }
}