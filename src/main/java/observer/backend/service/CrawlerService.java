package observer.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import observer.backend.entity.Category;
import observer.backend.entity.Product;
import observer.backend.repository.CategoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
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
@AllArgsConstructor
@Slf4j
public class CrawlerService {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    private static final Map<String, String> categoryUrls = Map.ofEntries(
        Map.entry("상의", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=001&caller=CATEGORY&page=%d&size=30"),
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

    @PostConstruct
    public void initCategories() {
        if (categoryRepository.count() == 0) {
            for (String category : categoryUrls.keySet()) {
                categoryRepository.save(new Category(category));
            }
        }
        log.info("카테고리 초기화 완료!");
    }

    public List<String[]> ajaxCrawling(String category, String baseUrl) {
        List<String[]> result = new ArrayList<>();
        log.info("{} 카테고리 크롤링 시작...", category);

        try {
            for (int page = 1; page <= 10; page++) { // 최대 10페이지
                String url = String.format(baseUrl, page);
                log.debug("Requesting URL: {}", url);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

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
                            item.get("saleRate").getAsString(),
                            String.valueOf(item.get("normalPrice").getAsInt()),
                            item.get("goodsLinkUrl").getAsString(),
                            item.get("thumbnail").getAsString()
                        });
                    }
                } else {
                    log.warn("HTTP request failed. Status Code: {}", conn.getResponseCode());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while crawling {} category", category, e);
        }

        log.info("{} 카테고리 크롤링 종료. 총 {}개의 상품이 수집되었습니다.", category, result.size());
        return result;
    }

    public List<String[]> parallelCrawling() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<List<String[]>>> futures = new ArrayList<>();

        for (Map.Entry<String, String> entry : categoryUrls.entrySet()) {
            String category = entry.getKey();
            String baseUrl = entry.getValue();
            futures.add(executorService.submit(() -> ajaxCrawling(category, baseUrl)));
        }

        List<String[]> allResults = new ArrayList<>();
        try {
            for (Future<List<String[]>> future : futures) {
                allResults.addAll(future.get());
            }
        } catch (Exception e) {
            log.error("Error during parallel crawling", e);
        } finally {
            executorService.shutdown();
        }

        log.info("병렬 크롤링 완료. 총 {}개의 상품이 수집되었습니다.", allResults.size());
        return allResults;
    }

    @Scheduled(cron = "0 10 17 * * ?") // 매일 오후 5시 30분 실행
    public void scheduleCrawling() {
        log.info("크롤링 스케줄러 실행 시작...");
        productService.createProduct(parallelCrawling());
        log.info("크롤링 스케줄러 실행 완료.");
    }
}
