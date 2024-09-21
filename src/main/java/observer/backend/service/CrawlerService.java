package observer.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class CrawlerService {

    // 카테고리별 URL 딕셔너리 (페이지 번호는 %d로 대체)
    private static final Map<String, String> categoryUrls = Map.ofEntries(
            Map.entry("상의", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=001&caller=CATEGORY&page=%d&size=30"),
            Map.entry("아우터", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=002&caller=CATEGORY&page=%d&size=30"),
            Map.entry("바지", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=003&caller=CATEGORY&page=%d&size=30"),
            Map.entry("원피스/스커트", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=100&caller=CATEGORY&page=%d&size=30"),
            Map.entry("신발", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=103&caller=CATEGORY&page=%d&size=30"),
            Map.entry("가방", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=004&caller=CATEGORY&page=%d&size=30"),
            Map.entry("패션소품", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=101&caller=CATEGORY&page=%d&size=30"),
            Map.entry("속옷/홈웨어", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=026&caller=CATEGORY&page=%d&size=30"),
            Map.entry("뷰티", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=104&caller=CATEGORY&page=%d&size=30"),
            Map.entry("스포츠/레저", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=017&caller=CATEGORY&page=%d&size=30"),
            Map.entry("디지털/라이브", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=102&caller=CATEGORY&page=%d&size=30"),
            Map.entry("아웃렛", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=107&caller=CATEGORY&page=%d&size=30"),
            Map.entry("부티크", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=105&caller=CATEGORY&page=%d&size=30"),
            Map.entry("키즈", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=106&caller=CATEGORY&page=%d&size=30"),
            Map.entry("어스", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=108&caller=CATEGORY&page=%d&size=30")
    );


    // 개별 카테고리에 대한 크롤링 작업 수행
    public List<String[]> ajaxCrawling(String category, String baseUrl) {
        List<String[]> result = new ArrayList<>();
        System.out.println(category + " 카테고리 크롤링 시작...");
        int n = 5; // n개의 페이지, 1페이지당 30개의 상품 존재, 모든상품 크롤링시에는 9999999 주면됨, 데이터 없으면 알아서 끊김

        try {
            for (int page = 1; page < n; page++) { // 각 카테고리에서 최대 n 페이지까지 크롤링
                // URL에 페이지 번호 삽입 (기존 {} 대신 %d 사용)
                String url = String.format(baseUrl, page);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                // HTTP 요청 응답 상태 코드 확인
                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // JSON 파싱
                    JsonObject document = JsonParser.parseString(response.toString()).getAsJsonObject();
                    JsonArray items = document.getAsJsonObject("data").getAsJsonArray("list");

                    for (JsonElement itemElement : items) {
                        JsonObject item = itemElement.getAsJsonObject();
                        result.add(new String[]{
                                String.valueOf(item.get("goodsNo").getAsInt()),
                                category,
                                item.get("brandName").getAsString(),
                                item.get("goodsName").getAsString(),
                                String.valueOf(item.get("price").getAsInt()),
                                item.get("saleRate").getAsString() + "%",
                                String.valueOf(item.get("normalPrice").getAsInt()),
                                item.get("goodsLinkUrl").getAsString(),
                                item.get("thumbnail").getAsString()
                        });
                    }
                } else {
                    System.out.println("HTTP request failed with status code: " + conn.getResponseCode());
                    break; // 상태 코드가 200이 아닌 경우 크롤링 중단
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(category + " 카테고리 크롤링 종료. 총 " + result.size() + "개의 상품이 수집되었습니다.");
        return result;
    }

    // 병렬 크롤링 작업 수행
    public List<String[]> parallelCrawling() {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 스레드 풀 설정
        List<Future<List<String[]>>> futures = new ArrayList<>();

        // 카테고리별로 병렬 작업 실행
        for (Map.Entry<String, String> entry : categoryUrls.entrySet()) {
            String category = entry.getKey();
            String baseUrl = entry.getValue();

            // 병렬 작업 실행 (각 카테고리마다 ajaxCrawling 호출)
            futures.add(executorService.submit(() -> ajaxCrawling(category, baseUrl)));
        }

        List<String[]> allResults = new ArrayList<>();
        try {
            // 각 병렬 작업 결과를 병합
            for (Future<List<String[]>> future : futures) {
                allResults.addAll(future.get()); // 병렬 작업의 결과를 추가
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // 스레드 풀 종료
        }

        return allResults; // 모든 카테고리의 결과를 반환
    }
}
