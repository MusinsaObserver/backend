package observer.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import observer.backend.entity.PriceHistory;

import java.time.LocalDate;

@Getter
public class PriceHistoryDto {
	private Long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate date;

	private Integer price;

	public PriceHistoryDto(PriceHistory priceHistory) {
		this.id = priceHistory.getId();
		this.date = priceHistory.getDate();
		this.price = priceHistory.getPrice();
	}
}
