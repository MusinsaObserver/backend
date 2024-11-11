package observer.backend.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {
	private String message;
	private T data;
	private PaginationInfo pagination;

	public ApiResponse(String message, T data, PaginationInfo pagination) {
		this.message = message;
		this.data = data;
		this.pagination = pagination;
	}

	public static <T> ApiResponse<List<T>> ok(String message, Page<T> pageData) {
		PaginationInfo paginationInfo = new PaginationInfo(
			pageData.getNumber(),
			pageData.getTotalPages(),
			pageData.getSize(),
			pageData.getTotalElements(),
			pageData.isLast()
		);
		return new ApiResponse<>(message, pageData.getContent(), paginationInfo);
	}

	public static <T> ApiResponse<List<T>> ok(String message, List<T> data) {
		return new ApiResponse<>(message, data, null);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(message, data, null);
	}


	public static <T> ApiResponse<T> fail(String message, ErrorResponse errorResponse) {
		return new ApiResponse<>(message, (T) errorResponse, null);
	}

	@Getter
	@Builder
	public static class PaginationInfo {
		private int currentPage;
		private int totalPages;
		private int pageSize;
		private long totalElements;
		private boolean isLast;

		public PaginationInfo(int currentPage, int totalPages, int pageSize, long totalElements, boolean isLast) {
			this.currentPage = currentPage;
			this.totalPages = totalPages;
			this.pageSize = pageSize;
			this.totalElements = totalElements;
			this.isLast = isLast;
		}
	}
}
