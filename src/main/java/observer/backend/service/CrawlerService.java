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

@Service
@AllArgsConstructor
@Slf4j
public class CrawlerService {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    private static final Map<String, String> categoryUrls = Map.ofEntries(
            Map.entry("상의", "https://api.musinsa.com/api2/dp/v1/plp/goods?gf=A&category=001&caller=CATEGORY&page=%d&size=30")
    );

    @PostConstruct
    public void initCategories() {
        log.info("Initializing categories...");
        if (categoryRepository.count() == 0) {
            for (String category : categoryUrls.keySet()) {
                categoryRepository.save(new Category(category));
                log.info("Category saved: {}", category);
            }
        }
        log.info("Category initialization completed.");
    }

    public void sequentialCrawling() {
        log.info("Starting sequential crawling...");
        for (String category : categoryUrls.keySet()) {
            String baseUrl = categoryUrls.get(category);
            List<String[]> results = ajaxCrawling(category, baseUrl);
            saveProductsInBatches(results, 50); // 배치 크기 50
        }
        log.info("Sequential crawling completed.");
    }

    public List<String[]> ajaxCrawling(String category, String baseUrl) {
        List<String[]> result = new ArrayList<>();
        log.info("Starting crawling for category: {}", category);

        try {
            for (int page = 1; ; page++) {
                String url = String.format(baseUrl, page);
                log.debug("Requesting URL: {}", url);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                int responseCode = conn.getResponseCode();

                if (responseCode != 200) {
                    log.warn("HTTP request failed. Status Code: {}", responseCode);
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

                // 데이터가 없으면 종료
                if (items.size() == 0) {
                    log.info("No more items for category: {} at page {}", category, page);
                    break;
                }

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

                // 요청 간 딜레이 추가 (500ms)
                Thread.sleep(500);
            }
        } catch (Exception e) {
            log.error("Error occurred while crawling category: {}", category, e);
        }

        log.info("Crawling completed for category: {}. Total items: {}", category, result.size());
        return result;
    }

    public void saveProductsInBatches(List<String[]> products, int batchSize) {
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

    @Scheduled(cron = "0 31 17 * * ?") // 매일 아침 6시에 실행
    public void scheduleCrawling() {
        log.info("Scheduled crawling started...");
        try {
            sequentialCrawling();
            log.info("Scheduled crawling completed.");
        } catch (Exception e) {
            log.error("Error during scheduled crawling", e);
        }
    }
}
