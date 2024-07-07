package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.ResponseIndexing;
import searchengine.dto.search.ResponseSearch;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<ResponseIndexing> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<ResponseIndexing> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }
    @PostMapping("/indexPage")
    public ResponseEntity<ResponseIndexing> pageIndexing(@RequestParam String url) {
        return ResponseEntity.ok(indexingService.pageIndexing(url));
    }
    @GetMapping("/search")
    public ResponseEntity<ResponseSearch> search(@RequestParam String query, @RequestParam String site,
                                                 @RequestParam String offset, @RequestParam String limit) {
        return ResponseEntity.ok(searchService.search(query, site,
                                                      offset, limit));
    }
}
