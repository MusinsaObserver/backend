package observer.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import observer.backend.entity.Category;
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

    // 무신사 카테고리 URL 목록
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
            Map.entry("디지털/라이브", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=102&caller=CATEGORY&page=%d&size=30")
    );

    @PostConstruct
    public void initCategories() {
        log.info("Initializing categories...");
        if (categoryRepository.count() == 0) {
            for (String category : categoryUrls.keySet()) {
                try {
                    categoryRepository.save(new Category(category));
                    log.info("Category saved: {}", category);
                } catch (Exception e) {
                    log.error("Error saving category: {}", category, e);
                }
            }
        }
        log.info("Category initialization completed.");
    }

    public List<String[]> ajaxCrawling(String category, String baseUrl) {
        List<String[]> result = new ArrayList<>();
        log.info("Starting crawling for category: {}", category);

        try {
            for (int page = 1; ; page++) {
                String url = String.format(baseUrl, page);
                log.debug("Requesting URL for page {}: {}", page, url);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

                int responseCode = conn.getResponseCode();
                log.debug("Response Code for category {}: {}", category, responseCode);

                if (responseCode != 200) {
                    log.warn("HTTP request failed for category {} on page {}. Status Code: {}", category, page, responseCode);
                    break;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject document = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray items = document.getAsJsonObject("data").getAsJsonArray("list");

                if (items.size() == 0) {
                    log.info("No more items for category: {} at page {}", category, page);
                    break;
                }

                for (JsonElement itemElement : items) {
                    JsonObject item = itemElement.getAsJsonObject();
                    try {
                        String[] parsedItem = new String[]{
                                String.valueOf(item.get("goodsNo").getAsInt()),
                                category,
                                item.get("brandName").getAsString(),
                                item.get("goodsName").getAsString(),
                                String.valueOf(item.get("price").getAsInt()),
                                item.get("saleRate").getAsString(),
                                String.valueOf(item.get("normalPrice").getAsInt()),
                                item.get("goodsLinkUrl").getAsString(),
                                item.get("thumbnail").getAsString()
                        };
                        result.add(parsedItem);
                        log.debug("Parsed item: {}", (Object) parsedItem);
                    } catch (Exception e) {
                        log.error("Error parsing item for category {}: {}", category, e.getMessage(), e);
                    }
                }

                // 요청 간 딜레이 추가 (200ms)
                Thread.sleep(200);
            }
        } catch (Exception e) {
            log.error("Error occurred while crawling category: {}", category, e);
        }

        log.info("Crawling completed for category: {}. Total items: {}", category, result.size());
        return result;
    }

    public List<String[]> parallelCrawling() {
        log.info("Starting parallel crawling for all categories...");
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 스레드 풀 크기: 3
        List<Future<List<String[]>>> futures = new ArrayList<>();

        for (String category : categoryUrls.keySet()) {
            String baseUrl = categoryUrls.get(category);
            futures.add(executorService.submit(() -> ajaxCrawling(category, baseUrl)));
        }

        List<String[]> allResults = new ArrayList<>();
        try {
            for (Future<List<String[]>> future : futures) {
                try {
                    List<String[]> categoryResults = future.get();
                    log.debug("Category crawling result size: {}", categoryResults.size());
                    allResults.addAll(categoryResults);
                } catch (Exception e) {
                    log.error("Error in parallel task", e);
                }
            }
        } finally {
            executorService.shutdown();
        }

        log.info("Parallel crawling for all categories completed. Total items: {}", allResults.size());
        return allResults;
    }

    @Scheduled(cron = "0 30 19 * * ?") // 매일 18시 40분 실행
    public void scheduleCrawling() {
        log.info("Scheduled crawling started...");
        try {
            saveProductsInBatches(parallelCrawling(), 50); // 배치 크기: 50
            log.info("Scheduled crawling completed.");
        } catch (Exception e) {
            log.error("Error during scheduled crawling", e);
        }
    }

    public void saveProductsInBatches(List<String[]> products, int batchSize) {
        log.info("Saving products in batches...");
        for (int i = 0; i < products.size(); i += batchSize) {
            List<String[]> batch = products.subList(i, Math.min(products.size(), i + batchSize));
            try {
                productService.createProduct(batch);
                log.info("Saved batch of {} products", batch.size());
            } catch (Exception e) {
                log.error("Error saving batch of products", e);
            }
        }
    }
}
