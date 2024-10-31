package observer.backend.dto;

import lombok.Getter;
import observer.backend.entity.PriceHistory;
import java.time.LocalDate;

@Getter
public class PriceHistoryDto {
	private LocalDate date;
	private Integer price;

	public PriceHistoryDto(PriceHistory priceHistory) {
		this.date = priceHistory.getDate();
		this.price = priceHistory.getPrice();
	}
}
