package observer.backend.service;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class PushNotificationService {

	private final HttpClient httpClient;

	public PushNotificationService() {
		this.httpClient = HttpClient.newHttpClient();
	}

	public void sendNotification(String deviceToken, String title, String body) {
		String apnsUrl = "https://api.sandbox.push.apple.com/3/device/" + deviceToken; // 개발용; 프로덕션에선 "https://api.push.apple.com/3/device/" 사용
		String payload = String.format("{\"aps\":{\"alert\":{\"title\":\"%s\",\"body\":\"%s\"},\"sound\":\"default\"}}", title, body);

		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(apnsUrl))
				.header("apns-topic", "com.example.Observer")  // App bundle ID
				.header("authorization", "bearer [your_apns_token]")  // JWT token
				.POST(HttpRequest.BodyPublishers.ofString(payload))
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				System.err.println("Failed to send push notification: " + response.body());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
