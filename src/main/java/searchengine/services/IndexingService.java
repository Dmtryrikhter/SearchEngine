package searchengine.services;

import searchengine.dto.indexing.ResponseIndexing;

public interface IndexingService {
    ResponseIndexing startIndexing();
    ResponseIndexing stopIndexing();
    ResponseIndexing pageIndexing(String url);
}
