package observer.backend.dto;

import lombok.Getter;
import java.time.LocalDate;
import observer.backend.entity.PriceHistory;

@Getter
public class PriceHistoryDto {
	private Long id;
	private LocalDate date;
	private Integer price;

	// Constructor to create a DTO from an entity
	public PriceHistoryDto(PriceHistory priceHistory) {
		this.id = priceHistory.getId();
		this.date = priceHistory.getDate();
		this.price = priceHistory.getPrice();
	}
}
