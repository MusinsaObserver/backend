package observer.backend.controller;

import observer.backend.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @GetMapping("/crawl")
    public List<String[]> startCrawling() {
        // 병렬 크롤링 실행
        return crawlerService.parallelCrawling();
    }
}
