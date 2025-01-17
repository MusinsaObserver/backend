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
        log.info("Initializing categories...");
        if (categoryRepository.count() == 0) {
            for (String category : categoryUrls.keySet()) {
                categoryRepository.save(new Category(category));
                log.info("Category saved: {}", category);
            }
        }
        log.info("Category initialization completed.");
    }

    public List<String[]> ajaxCrawling(String category, String baseUrl) {
        List<String[]> result = new ArrayList<>();
        log.info("Starting crawling for category: {}", category);

        try {
            for (int page = 1; page <= 10; page++) {
                String url = String.format(baseUrl, page);
                log.debug("Requesting URL: {}", url);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

                int responseCode = conn.getResponseCode();
                log.debug("Response Code: {}", responseCode);

                if (responseCode == 200) {
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
                    log.warn("HTTP request failed. Status Code: {}", responseCode);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while crawling category: {}", category, e);
        }

        log.info("Crawling completed for category: {}. Total items: {}", category, result.size());
        return result;
    }

    public List<String[]> parallelCrawling() {
    log.info("Starting parallel crawling for all categories...");
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    List<Future<List<String[]>>> futures = new ArrayList<>();

    // 모든 카테고리를 선택
    List<String> selectedCategories = new ArrayList<>(categoryUrls.keySet());

    for (String category : selectedCategories) {
        String baseUrl = categoryUrls.get(category);
        futures.add(executorService.submit(() -> ajaxCrawling(category, baseUrl)));
    }

    List<String[]> allResults = new ArrayList<>();
    try {
        for (Future<List<String[]>> future : futures) {
            try {
                allResults.addAll(future.get());
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

    @Scheduled(cron = "0 56 18 * * ?")
    public void scheduleCrawling() {
        log.info("Scheduled crawling started...");
        try {
            saveProductsInBatches(parallelCrawling(), 100);
            log.info("Scheduled crawling completed.");
        } catch (Exception e) {
            log.error("Error during scheduled crawling", e);
        }
    }
    public void saveProductsInBatches(List<String[]> products, int batchSize) {
        for (int i = 0; i < products.size(); i += batchSize) {
            List<String[]> batch = products.subList(i, Math.min(products.size(), i + batchSize));
            productService.createProduct(batch);
            log.info("Saved batch of {} products", batch.size());
        }
    }
}
