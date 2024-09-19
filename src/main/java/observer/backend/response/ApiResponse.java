package observer.backend.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse< T > {
	private String message;
	private T data;

	public ApiResponse( String message, T data) {
		this.message = message;
		this.data = data;
	}

	public static < T > ApiResponse< T > ok( String message, T data ) {
		return new ApiResponse< T >(
			message,
			data
		);
	}

	public static < T > ApiResponse< T > fail( String message, T errorResponse ) {
		return new ApiResponse< T >(
			message,
			errorResponse
		);
	}
}
